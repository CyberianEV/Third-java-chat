package org.chat.client;

import org.chat.common.Messages;
import org.chat.network.SocketThread;
import org.chat.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class Client extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private static final String TITLE = "Chat Client";
    private final JTextArea log = new JTextArea();

    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("8189");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField tfLogin = new JTextField("Donald");
    private final JPasswordField tfPassword = new JPasswordField("123");
    private final JButton btnLogin = new JButton("Login");

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("<html><b>Send</b></html>");
    private final JList<String> userList = new JList<>();

    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu mUser = new JMenu("User");
    private final JMenuItem miSignUp = new JMenuItem("Sign up");
    private final JMenuItem miChangePassword = new JMenuItem("Change Password");
    private final JMenuItem miChangeName = new JMenuItem("Change Name");

    private FileWriter logWriter = null;
    private File logFile = null;
    private boolean shownIoErrors = false;
    private SocketThread socketThread;
    private PassChanger passChanger;
    private NameChanger nameChanger;


    private Client() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); //in the middle of the screen
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);
        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane spLog = new JScrollPane(log);
        JScrollPane spUsers = new JScrollPane(userList);
        spUsers.setPreferredSize(new Dimension(100, 0));
        cbAlwaysOnTop.addActionListener(this);
        btnSend.addActionListener(this);
        tfMessage.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);
        miSignUp.addActionListener(this);
        miChangeName.addActionListener(this);
        miChangePassword.addActionListener(this);
        panelBottom.setVisible(false);
        miChangeName.setEnabled(false);
        miChangePassword.setEnabled(false);

        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);

        mUser.add(miSignUp);
        mUser.addSeparator();
        mUser.add(miChangeName);
        mUser.add(miChangePassword);
        menuBar.add(mUser);

        add(panelBottom, BorderLayout.SOUTH);
        add(panelTop, BorderLayout.NORTH);
        add(spLog, BorderLayout.CENTER);
        add(spUsers, BorderLayout.EAST);
        setJMenuBar(menuBar);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnSend || src == tfMessage) {
            sendMessage();
        } else if (src == btnLogin) {
            connect();
        } else if (src == btnDisconnect) {
            socketThread.close();
        } else if (src == miSignUp) {
            new SignUp(this);
        } else if (src == miChangePassword) {
            passChanger = new PassChanger(this);
        } else if (src == miChangeName) {
            nameChanger = new NameChanger(this);
        } else {
            throw new RuntimeException("Action for component unimplemented");
        }
    }

    public void connect() {
        try {
            Socket socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
            socketThread = new SocketThread(this, "Client", socket);
            socketThread.start();
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    public String getTfIPAddress() {
        return tfIPAddress.getText();
    }

    public int getTfPort() {
        return Integer.parseInt(tfPort.getText());
    }

    public boolean isConnected() {
        return socketThread != null || socketThread.isAlive();
    }

    private void sendMessage() {
        String msg = tfMessage.getText();
        String username = tfLogin.getText();
        if ("".equals(msg)) return;
        tfMessage.setText(null);
        tfMessage.grabFocus();
        socketThread.sendMessage(Messages.getTypeBcastFromClient(msg));
//        tfMessage.requestFocusInWindow();
//        putLog(String.format("%s: %s", username, msg));
        //wrtMsgToLogFile(msg, username);
    }

    public void sendPassChange(String newPass, String oldPass) {
        String login = tfLogin.getText();
        socketThread.sendMessage(Messages.getPassChangeRequest(newPass, oldPass, login));
    }

    public void sendNameChange(String name) {
        String login = tfLogin.getText();
        socketThread.sendMessage(Messages.getNameChangeRequest(name, login));
    }

    private void wrtMsgToLogFile(String msg, String username) {
        try (FileWriter out = new FileWriter("log.txt", true)) {
            out.write(username + ": " + msg + "\n");
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e);
            }
        }
    }

    private void writeToLogFile(String msg) {
        try {
            logWriter.write(msg);
            logWriter.flush();
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    public void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    private void showException(Thread t, Throwable e) {
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0)
            msg = "Empty Stacktrace";
        else {
            msg = String.format("Exception in \"%s\" %s: %s\n\tat %s",
                    t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
            JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        }
        //JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
    }

    private void createLogFile (String filename) {
        String fileFullName = filename + ".txt";
        logFile = new File(fileFullName);
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    private void createWriterStream(File file) {
        try {
            logWriter = new FileWriter(file, true);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    private void closeWriterStream() {
        if (logWriter != null) {
            try {
                logWriter.close();
            } catch (IOException e) {
                showException(Thread.currentThread(), e);
            }
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        showException(t, e);
    }

    @Override
    public void onSocketStart(SocketThread t, Socket s) {
//        putLog("Start");
    }

    @Override
    public void onSocketStop(SocketThread t) {
        panelBottom.setVisible(false);
        panelTop.setVisible(true);
        miSignUp.setEnabled(true);
        miChangeName.setEnabled(false);
        miChangePassword.setEnabled(false);
        setTitle(TITLE);
        userList.setListData(new String[0]);
        closeWriterStream();
    }

    @Override
    public void onSocketReady(SocketThread t, Socket socket) {
        panelBottom.setVisible(true);
        panelTop.setVisible(false);
        miSignUp.setEnabled(false);
        miChangeName.setEnabled(true);
        miChangePassword.setEnabled(true);
        String login = tfLogin.getText();
        String pass = new String(tfPassword.getPassword());
        t.sendMessage(Messages.getAuthRequest(login, pass));
    }

    @Override
    public void onReceiveString(SocketThread t, Socket s, String msg) {
        handleMessage(msg);
    }

    void handleMessage(String value) {
        String[] arr = value.split(Messages.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Messages.AUTH_ACCEPT:
                setTitle(TITLE + " logged in as: " + arr[1]);
                createLogFile("history_" + tfLogin.getText());
                createWriterStream(logFile);
                break;
            case Messages.AUTH_DENY:
                putLog("Access Denied");
                break;
            case Messages.MSG_FORMAT_ERROR:
                putLog(value);
                socketThread.close();
                break;
            case Messages.USER_LIST:
                String users = value.substring(Messages.DELIMITER.length() +
                        Messages.USER_LIST.length());
                String[] usersArr = users.split(Messages.DELIMITER);
                Arrays.sort(usersArr);
                userList.setListData(usersArr);
                break;
            case Messages.MSG_BROADCAST:
                String msg = DATE_FORMAT.format(Long.parseLong(arr[1])) + arr[2] + ": " + arr[3] + "\n";
                log.append(msg);
                log.setCaretPosition(log.getDocument().getLength());
                writeToLogFile(msg);
                break;
            case Messages.PASSCHANGE_SUCCEED:
                value = arr[1];
                putLog(value);
                passChanger.dispose();
                break;
            case Messages.NAMECHANGE_SUCCEED:
                nameChanger.dispose();
                break;
            case Messages.FAILED:
                value = arr[1];
                JOptionPane.showMessageDialog(this, value, "Something went wrong", JOptionPane.ERROR_MESSAGE);
                break;
            case Messages.HISTORY_LOG:
                log.append(arr[1] + "\n");
                log.setCaretPosition(log.getDocument().getLength());
                break;
            default:
                throw new RuntimeException("Unknown message type: " + msgType);
        }
    }

    @Override
    public void onSocketException(SocketThread t, Throwable e) {

    }
}
