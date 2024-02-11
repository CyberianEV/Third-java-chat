package org.chat.client;

import org.chat.common.Messages;
import org.chat.network.SocketThread;
import org.chat.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class SignUp extends JDialog implements ActionListener, SocketThreadListener {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 130;
    private static final String TITLE = "Sign up";

    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField tfNickname = new JTextField();
    private final JTextField tfLogin = new JTextField();
    private final JPasswordField tfPassword = new JPasswordField();
    private final JLabel lName = new JLabel("Name", SwingConstants.CENTER);
    private final JLabel lLogin = new JLabel("Login", SwingConstants.CENTER);
    private final JLabel lPassword = new JLabel("Password", SwingConstants.CENTER);

    private final JPanel panelBottom = new JPanel(new GridLayout(1, 2));
    private final JButton btnSignUp = new JButton("Sign up");
    private final JButton btnCancel = new JButton("Cancel");

    private final   JLabel lMessage = new JLabel();

    private Client client;
    private SocketThread socketThread;

    public SignUp(Client client) {
        super(client, TITLE, true);
        this.client = client;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(client);
        setResizable(false);
        setAlwaysOnTop(true);
        btnCancel.addActionListener(this);
        btnSignUp.addActionListener(this);

        panelTop.add(lName);
        panelTop.add(lLogin);
        panelTop.add(lPassword);
        panelTop.add(tfNickname);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);

        panelBottom.add(btnCancel);
        panelBottom.add(btnSignUp);

        add(panelTop, BorderLayout.NORTH);
        add(lMessage, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.PAGE_END);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnCancel) {
            closeDialog();
        } else if (src == btnSignUp) {
            if (!isConnected()) {
                connect();
            } else {
                sendSignup(socketThread);
            }
        } else {
            throw new RuntimeException("Action for component unimplemented");
        }
    }

    public boolean isConnected() {
        return (socketThread != null && socketThread.isAlive());
    }

    private void closeDialog() {
        dispose();
        if (isConnected()) {
            socketThread.close();
        }
    }

    private void connect() {
        try {
            Socket socket = new Socket(client.getTfIPAddress(), client.getTfPort());
            socketThread = new SocketThread(this, "Sign Up Client", socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendSignup(SocketThread t) {
        String login = tfLogin.getText();
        String pass = new String(tfPassword.getPassword());
        String nickname = tfNickname.getText();
        t.sendMessage(Messages.getSignupRequest(login, pass, nickname));
    }

    @Override
    public void onSocketStart(SocketThread t, Socket s) {

    }

    @Override
    public void onSocketStop(SocketThread t) {

    }

    @Override
    public void onSocketReady(SocketThread t, Socket socket) {
        sendSignup(t);
    }

    @Override
    public void onReceiveString(SocketThread t, Socket s, String msg) {
        handleMessage(msg);
    }

    private void handleMessage(String msg) {
        String[] arr = msg.split(Messages.DELIMITER);
        String msgType = arr[0];
        String value;
        switch (msgType) {
            case Messages.SIGNUP_SUCCEED:
                value = arr[1];
                client.putLog(value);
                closeDialog();
                break;
            case Messages.FAILED:
                value = arr[1];
                lMessage.setText(value);
                break;
            default:
                throw new RuntimeException("Unknown message type: " + msgType);
        }
    }

    @Override
    public void onSocketException(SocketThread t, Throwable e) {

    }
}
