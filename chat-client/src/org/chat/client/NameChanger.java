package org.chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NameChanger extends JDialog implements ActionListener {
    private static final int WIDTH = 200;
    private static final int HEIGHT = 100;
    private static final String TITLE = "Change name";

    private final JTextField tfName = new JTextField();

    private final JPanel panelBottom = new JPanel(new GridLayout(1, 2));
    private final JButton btnApply = new JButton("Apply");
    private final JButton btnCancel = new JButton("Cancel");

    private final JLabel lMessage = new JLabel();

    private Client client;

    public NameChanger(Client client) {
        super(client, TITLE, false);
        this.client = client;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(client);
        setResizable(false);
        setAlwaysOnTop(true);
        btnCancel.addActionListener(this);
        btnApply.addActionListener(this);

        panelBottom.add(btnCancel);
        panelBottom.add(btnApply);

        add(tfName, BorderLayout.NORTH);
        add(lMessage, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.PAGE_END);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnCancel) {
            dispose();
        } else if (src == btnApply) {
            String name = tfName.getText();
            if (areFilledCorrectly(name)) {
                client.sendNameChange(name);
            }
        } else {
            throw new RuntimeException("Action for component unimplemented");
        }

    }

    private boolean areFilledCorrectly(String name) {
        if (name.isEmpty()) {
            putMessage("All fields must be filled out");
            return false;
        } else {
            return true;
        }
    }

    private void putMessage(String msg) {
        lMessage.setText("<html>" + msg);
    }
}
