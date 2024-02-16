package org.chat.server.core;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.chat.common.Messages;
import org.chat.network.ServerSocketThread;
import org.chat.network.ServerSocketThreadListener;
import org.chat.network.SocketThread;
import org.chat.network.SocketThreadListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener, ThreadFactory {
    private final int SERVER_SOCKET_TIMEOUT = 2000;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private Vector<SocketThread> clients = new Vector<>();

    int counter = 0;
    ServerSocketThread server;
    ChatServerListener listener;
    private ExecutorService executorService;
    private String clientThreadName;

    private FileWriter historyLogWriter = null;
    private File historyLogFile = null;

    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (server != null && server.isAlive()) {
            putLog("Server already started");
        } else {
            server = new ServerSocketThread(this, "Chat server " + counter++, port, SERVER_SOCKET_TIMEOUT);
        }
    }

    public void stop() {
        if (server == null || !server.isAlive()) {
            putLog("Server is not running");
        } else {
            server.interrupt();
        }
    }

    private void putLog(String msg) {
        msg = DATE_FORMAT.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() +
                ": " + msg;
        listener.onChatServerMessage(msg);
    }

    private void createHistoryLogFile () {
        historyLogFile = new File("server_history_log.txt");
        try {
            historyLogFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createWriterStream(File file) {
        try {
            historyLogWriter = new FileWriter(file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWriterStream() {
        if (historyLogWriter != null) {
            try {
                historyLogWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeToLogFile(String src, String msg) {
        String textLine = DATE_FORMAT.format(System.currentTimeMillis()) + src + ": " + msg + "\n";
        try {
            historyLogWriter.write(textLine);
            historyLogWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendChatHistory (ClientThread client) {
        ArrayList<String> reversedLogLines = getLastLinesFromFile(historyLogFile, 100);
        for (int i = 99; i >= 0; i--) {
            client.sendMessage(Messages.getHistoryLog(reversedLogLines.get(i)));
        }
    }

    private ArrayList<String> getLastLinesFromFile(File file, int amount) {
        ArrayList<String> reversedLogLines = new ArrayList<>();
        try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                .setFile(file)
                .setBufferSize(4096)
                .setCharset(StandardCharsets.UTF_8)
                .get()) {
            int counter = amount;
            while(counter > 0) {
                reversedLogLines.add(reader.readLine());
                counter--;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reversedLogLines;
    }

    /**
     * Server socket thread methods
     * */

    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server thread started");
        SqlClient.connect();
        createHistoryLogFile();
        createWriterStream(historyLogFile);
        executorService = Executors.newCachedThreadPool(this);
    }

    @Override
    public synchronized void onServerStop(ServerSocketThread thread) {
        putLog("Server thread stopped");
        SqlClient.disconnect();
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
        closeWriterStream();
        executorService.shutdown();
    }

    @Override
    public void onServerSocketCreated(ServerSocketThread t, ServerSocket s) {
        putLog("Server socket created");
    }

    @Override
    public void onServerSoTimeout(ServerSocketThread t, ServerSocket s) {
        //
    }

    @Override
    public void onSocketAccepted(ServerSocketThread t, ServerSocket s, Socket client) {
        putLog("client connected");
        clientThreadName = "SocketThread" + client.getInetAddress() + ": " + client.getPort();
        executorService.execute(new ClientThread(this, clientThreadName, client));

    }

    @Override
    public void onServerException(ServerSocketThread t, Throwable e) {
        e.printStackTrace();
    }

    /**
     * Socket Thread listening
     * */

    @Override
    public synchronized void onSocketStart(SocketThread t, Socket s) {
        putLog("Client connected");
    }

    @Override
    public synchronized void onSocketStop(SocketThread t) {
        ClientThread client = (ClientThread) t;
        clients.remove(client);
        if (client.isAuthorized() && !client.isReconnecting()) {
            sendToAllAuthorized(Messages.getTypeBroadcast("Server", client.getNickname() + " disconnected"));
        }
        sendToAllAuthorized(Messages.getUserList(getUsers()));
        putLog("clients size is " + clients.size());
    }

    @Override
    public synchronized void onSocketReady(SocketThread t, Socket socket) {
        putLog("client is ready");
        clients.add(t);
        putLog("clients size is " + clients.size());
        ClientThread client = (ClientThread) t;
        Thread disconnectUnauthorizedTimer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!client.isAuthorized()) {
                    client.close();
                    onSocketStop(t);
                }
            }
        });
        disconnectUnauthorizedTimer.start();
    }

    @Override
    public synchronized void onReceiveString(SocketThread t, Socket s, String msg) {
        ClientThread client = (ClientThread) t;
        if (client.isAuthorized()) {
            handleAuthMsg(client, msg);
        } else {
            handleNonAuthMsg(client, msg);
        }
    }

    private void handleAuthMsg(ClientThread client, String msg) {
        String[] arr = msg.split(Messages.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Messages.USER_BROADCAST:
                sendToAllAuthorized(Messages.getTypeBroadcast(client.getNickname(), arr[1]));
                writeToLogFile(client.getNickname(), arr[1]);
                break;
            case Messages.PASSCHANGE_REQUEST:
                String newPass = arr[1];
                String oldPass = arr[2];
                String login = arr[3];
                if (newPass.length() < 3) {
                    client.sendMessage(Messages.getFailedPassLength());
                } else if (SqlClient.loginMatchesPass(login, oldPass)) {
                    boolean isIUpdated = SqlClient.changePass(login, newPass);
                    if (isIUpdated) {
                        client.sendMessage(Messages.getPasschangeSucceed(login));
                    } else {
                        client.sendMessage(Messages.getPasschangeFailed());
                    }
                } else {
                    client.sendMessage(Messages.getMismathedLoginPass());
                }
                break;
            case Messages.NAMECHANGE_REQUEST:
                String nickname = arr[1];
                String log_in = arr[2];
                if (!nickname.isEmpty()) {
                    boolean isIUpdated = SqlClient.changeName(log_in, nickname);
                    if (isIUpdated) {
                        client.sendMessage(Messages.NAMECHANGE_SUCCEED);
                        sendToAllAuthorized(Messages.getTypeBroadcast("Server", client.getNickname() + " now known as " + nickname));
                        client.setNickname(nickname);
                        sendToAllAuthorized(Messages.getUserList(getUsers()));
                    } else {
                        client.sendMessage(Messages.getNameChangeFailed());
                    }
                } else {
                    client.sendMessage(Messages.getFailedEmptyFields());
                }
                break;
            default:
                client.msgFormatError(msg);
        }
    }

    private void sendToAllAuthorized(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            client.sendMessage(msg);
        }
    }

    private void handleNonAuthMsg(ClientThread client, String msg) {
        String[] arr = msg.split(Messages.DELIMITER);
        String msgType = arr[0];
        String login;
        String password;
        String nickname;
        switch (msgType) {
            case Messages.AUTH_REQUEST:
                login = arr[1];
                password = arr[2];
                nickname = SqlClient.getNick(login, password);
                if (nickname == null) {
                    putLog("Invalid login attempt " + login);
                    client.authFail();
                    return;
                } else {
                    ClientThread oldClient = findClientByNickname(nickname);
                    client.authAccept(nickname);
                    sendChatHistory(client);
                    if (oldClient == null){
                        sendToAllAuthorized(Messages.getTypeBroadcast("Server", nickname + " connected."));
                    } else {
                        oldClient.reconnect();
                        clients.remove(oldClient);
                    }
                }
                sendToAllAuthorized(Messages.getUserList(getUsers()));
                break;
            case Messages.SIGNUP_REQUEST:
                if (arr.length == 4 && !arr[1].isEmpty() && !arr[2].isEmpty()) {
                    login = arr[1];
                    password = arr[2];
                    nickname = arr[3];
                    if (SqlClient.loginIsNotUnique(login)) {
                        client.sendMessage(Messages.getFailedUserUnunique(login));
                    } else if (password.length() < 3) {
                        client.sendMessage(Messages.getFailedPassLength());
                    } else {
                        boolean isInserted = SqlClient.signUp(login, password, nickname);
                        if (isInserted) {
                            client.sendMessage(Messages.getSignupSucceed(login));
                        } else {
                            client.sendMessage(Messages.getSignupFailed());
                        }
                    }
                } else {
                    client.sendMessage(Messages.getFailedEmptyFields());
                }
                break;

            default:
                client.msgFormatError(msg);
        }
    }

    @Override
    public synchronized void onSocketException(SocketThread t, Throwable e) {

    }

    private String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            sb.append(client.getNickname()).append(Messages.DELIMITER);
        }
        return sb.toString();
    }

    private synchronized ClientThread findClientByNickname(String nickname) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname))
                return client;
        }
        return null;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "clients.pool-" + clientThreadName);
    }
}
