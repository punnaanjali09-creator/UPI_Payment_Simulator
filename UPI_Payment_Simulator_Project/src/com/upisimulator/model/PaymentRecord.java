package com.upisimulator.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PaymentRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    private final String sender;
    private final String receiver;
    private final BigDecimal amount;
    private final TransactionType type;
    private TransactionStatus status;
    private final String note;
    private final LocalDateTime time = LocalDateTime.now();

    public PaymentRecord(String sender, String receiver, BigDecimal amount,
                         TransactionType type, TransactionStatus status, String note) {
        this.sender = sender; this.receiver = receiver; this.amount = amount;
        this.type = type; this.status = status; this.note = note;
    }
    public String getId() { return id; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public String getNote() { return note; }
    public LocalDateTime getTime() { return time; }
    @Override public String toString() {
        return time.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + " | " +
               type + " | " + sender + " -> " + receiver + " | Rs. " + amount +
               " | " + status + " | ID: " + id;
    }
}