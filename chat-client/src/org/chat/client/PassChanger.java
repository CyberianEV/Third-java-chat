package org.chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PassChanger extends JDialog implements ActionListener {
    private static final int WIDTH = 200;
    private static final int HEIGHT = 300;
    private static final String TITLE = "Change password";

    private final JPanel panelTop = new JPanel(new GridLayout(6, 1));
    private final JPasswordField tfNewPass = new JPasswordField();
    private final JPasswordField tfRepeatedPass = new JPasswordField();
    private final JPasswordField tfOldPass = new JPasswordField();
    private final JLabel lNewPass = new JLabel("Enter new password", SwingConstants.LEFT);
    private final JLabel lRepeatedPass = new JLabel("Repeat new password", SwingConstants.LEFT);
    private final JLabel lOldPass = new JLabel("Enter current password", SwingConstants.LEFT);

    private final JPanel panelBottom = new JPanel(new GridLayout(1, 2));
    private final JButton btnChangePass = new JButton("Apply");
    private final JButton btnCancel = new JButton("Cancel");

    private final JLabel lMessage = new JLabel();

    private Client client;

    public PassChanger(Client client) {
        super(client, TITLE, false);
        this.client = client;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(client);
        setResizable(false);
        setAlwaysOnTop(true);
        btnCancel.addActionListener(this);
        btnChangePass.addActionListener(this);

        panelTop.add(lNewPass);
        panelTop.add(tfNewPass);
        panelTop.add(lRepeatedPass);
        panelTop.add(tfRepeatedPass);
        panelTop.add(lOldPass);
        panelTop.add(tfOldPass);

        panelBottom.add(btnCancel);
        panelBottom.add(btnChangePass);

        add(panelTop, BorderLayout.NORTH);
        add(lMessage, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.PAGE_END);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnCancel) {
            dispose();
        } else if (src == btnChangePass) {
            String newPass = new String(tfNewPass.getPassword());
            String repeatedPass = new String(tfRepeatedPass.getPassword());
            String oldPass = new String(tfOldPass.getPassword());
            if (areFilledCorrectly(newPass, repeatedPass, oldPass)) {
                client.sendPassChange(newPass, oldPass);
            }
        } else {
            throw new RuntimeException("Action for component unimplemented");
        }
    }

    private boolean areFilledCorrectly(String newPass, String repeatedPass, String oldPass) {
        if (newPass.isEmpty() || repeatedPass.isEmpty() || oldPass.isEmpty()) {
            putMessage("All fields must be filled out");
            return false;
        } else if (!newPass.equals(repeatedPass)) {
            putMessage("The new password and the repeated new password must match");
            return false;
        } else {
            return true;
        }
    }

    private void putMessage(String msg) {
        lMessage.setText("<html>" + msg);
    }
}
