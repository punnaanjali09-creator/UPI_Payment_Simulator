package com.upisimulator.ui;

import com.upisimulator.model.*;
import com.upisimulator.service.PaymentService;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final PaymentService service;
    private final UpiUser user;
    private final JLabel balance = new JLabel();
    private final JTextArea output = new JTextArea(12, 65);

    public DashboardFrame(PaymentService service, UpiUser user) {
        this.service = service; this.user = user;
        setTitle("UPI Dashboard - " + user.getUpiId()); setSize(820, 570);
        setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(12,12)); root.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JLabel heading = new JLabel("Welcome, " + user.getFullName() + "  |  " + user.getUpiId());
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        JPanel top = new JPanel(new BorderLayout()); top.add(heading, BorderLayout.NORTH);
        balance.setFont(new Font("Arial", Font.BOLD, 20)); balance.setForeground(new Color(25,110,75));
        top.add(balance, BorderLayout.SOUTH); root.add(top, BorderLayout.NORTH);
        output.setEditable(false); output.setFont(new Font("Monospaced", Font.PLAIN, 12));
        root.add(new JScrollPane(output), BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(2,4,8,8));
        JButton send = new JButton("Send Money"), request = new JButton("Request Money");
        JButton history = new JButton("Transaction History"), mini = new JButton("Mini Statement");
        JButton requests = new JButton("My Requests"), users = new JButton("Demo Users");
        JButton refresh = new JButton("Refresh Balance"), logout = new JButton("Logout");
        for (JButton b : new JButton[]{send,request,history,mini,requests,users,refresh,logout}) actions.add(b);
        root.add(actions, BorderLayout.SOUTH); setContentPane(root);
        send.addActionListener(e -> sendMoney());
        request.addActionListener(e -> requestMoney());
        history.addActionListener(e -> showRecords(service.getTransactionsFor(user.getUpiId())));
        mini.addActionListener(e -> showRecords(service.getMiniStatement(user.getUpiId())));
        requests.addActionListener(e -> showRequests(service.getRequestsFor(user.getUpiId())));
        users.addActionListener(e -> showText("REGISTERED DEMO USERS\n" + String.join("\n", service.getRegisteredUsers())));
        refresh.addActionListener(e -> refreshBalance());
        logout.addActionListener(e -> { new LoginFrame(service).setVisible(true); dispose(); });
        refreshBalance();
    }
    private void refreshBalance() {
        try { balance.setText("Available virtual balance: Rs. " + service.getBalance(user.getUpiId())); }
        catch (Exception ex) { balance.setText(ex.getMessage()); }
    }
    private void sendMoney() {
        JTextField receiver = new JTextField(), amount = new JTextField(), pin = new JPasswordField();
        JPanel p = new JPanel(new GridLayout(3,2,8,8));
        p.add(new JLabel("Receiver UPI ID")); p.add(receiver);
        p.add(new JLabel("Amount (Rs.)")); p.add(amount);
        p.add(new JLabel("Your UPI PIN")); p.add(pin);
        if (JOptionPane.showConfirmDialog(this,p,"Send virtual money",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            try {
                BigDecimal value = new BigDecimal(amount.getText());
                JButton unused = new JButton();
                new SwingWorker<PaymentRecord,Void>() {
                    protected PaymentRecord doInBackground() throws Exception {
                        return service.sendMoney(user.getUpiId(), receiver.getText(), pin.getText(), value);
                    }
                    protected void done() {
                        try { PaymentRecord r = get(); showText("PAYMENT SUCCESSFUL\n" + r); refreshBalance(); }
                        catch (Exception ex) { showError(ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage()); refreshBalance(); }
                    }
                }.execute();
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }
    private void requestMoney() {
        JTextField payer = new JTextField(), amount = new JTextField();
        JPanel p = new JPanel(new GridLayout(2,2,8,8));
        p.add(new JLabel("Request from UPI ID")); p.add(payer);
        p.add(new JLabel("Amount (Rs.)")); p.add(amount);
        if (JOptionPane.showConfirmDialog(this,p,"Request money",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            try {
                PaymentRequest r = service.requestMoney(user.getUpiId(), payer.getText(), new BigDecimal(amount.getText()));
                showText("REQUEST CREATED\n" + r + "\nNo money was transferred.");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }
    private void showRecords(List<PaymentRecord> records) {
        StringBuilder s = new StringBuilder("TRANSACTION RECORDS\n\n");
        if (records.isEmpty()) s.append("No transactions available.");
        for (PaymentRecord r : records) s.append(r).append("\n");
        showText(s.toString());
    }
    private void showRequests(List<PaymentRequest> records) {
        StringBuilder s = new StringBuilder("MONEY REQUESTS\n\n");
        if (records.isEmpty()) s.append("No requests available.");
        for (PaymentRequest r : records) s.append(r).append("\n");
        showText(s.toString());
    }
    private void showText(String text) { output.setText(text); }
    private void showError(String message) { JOptionPane.showMessageDialog(this,message,"Operation failed",JOptionPane.ERROR_MESSAGE); }
}