package com.upisimulator.ui;

import com.upisimulator.model.UpiUser;
import com.upisimulator.service.PaymentService;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class LoginFrame extends JFrame {
    private final PaymentService service;
    private final JTextField upi = new JTextField(18);
    private final JPasswordField pin = new JPasswordField(18);

    public LoginFrame(PaymentService service) {
        this.service = service;
        setTitle("UPI Payment Simulator - Login");
        setSize(470, 350); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(15,15)); root.setBorder(BorderFactory.createEmptyBorder(25,30,25,30));
        JLabel title = new JLabel("UPI PAYMENT SIMULATOR", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22)); title.setForeground(new Color(30,75,130));
        root.add(title, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(4,2,10,12));
        form.add(new JLabel("UPI ID")); form.add(upi);
        form.add(new JLabel("UPI PIN")); form.add(pin);
        JButton login = new JButton("Login"); JButton register = new JButton("Create Account");
        form.add(login); form.add(register); root.add(form, BorderLayout.CENTER);
        JLabel note = new JLabel("Demo only • Virtual balances • No real bank connection", SwingConstants.CENTER);
        note.setForeground(Color.GRAY); root.add(note, BorderLayout.SOUTH);
        setContentPane(root);
        login.addActionListener(e -> doLogin());
        register.addActionListener(e -> doRegister());
    }
    private void doLogin() {
        try {
            UpiUser user = service.login(upi.getText(), new String(pin.getPassword()));
            new DashboardFrame(service, user).setVisible(true); dispose();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE); }
    }
    private void doRegister() {
        JTextField name = new JTextField(), newUpi = new JTextField(), newPin = new JTextField(), balance = new JTextField("5000");
        JPanel panel = new JPanel(new GridLayout(4,2,8,8));
        panel.add(new JLabel("Full name")); panel.add(name);
        panel.add(new JLabel("UPI ID (name@bank)")); panel.add(newUpi);
        panel.add(new JLabel("4-6 digit PIN")); panel.add(newPin);
        panel.add(new JLabel("Opening balance (Rs.)")); panel.add(balance);
        if (JOptionPane.showConfirmDialog(this, panel, "Create UPI account", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                service.register(name.getText(), newUpi.getText(), newPin.getText(), new BigDecimal(balance.getText()));
                JOptionPane.showMessageDialog(this, "Account created. You can now log in.");
                upi.setText(newUpi.getText()); pin.setText("");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration failed", JOptionPane.ERROR_MESSAGE); }
        }
    }
}