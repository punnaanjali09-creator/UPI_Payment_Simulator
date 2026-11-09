package com.upisimulator.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    private final String requesterUpi;
    private final String requestedFromUpi;
    private final BigDecimal amount;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private boolean paid;

    public PaymentRequest(String requesterUpi, String requestedFromUpi, BigDecimal amount) {
        this.requesterUpi = requesterUpi; this.requestedFromUpi = requestedFromUpi; this.amount = amount;
    }
    public String getRequesterUpi() { return requesterUpi; }
    public String getRequestedFromUpi() { return requestedFromUpi; }
    public BigDecimal getAmount() { return amount; }
    public String getRequestId() { return requestId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isPaid() { return paid; }
    public void markPaid() { paid = true; }
    @Override public String toString() {
        return "Request " + requestId + ": " + requesterUpi + " requested Rs. " + amount +
               " from " + requestedFromUpi + " [" + (paid ? "PAID" : "PENDING") + "]";
    }
}