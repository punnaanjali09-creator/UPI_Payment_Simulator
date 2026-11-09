package com.upisimulator.service;

import com.upisimulator.model.*;
import com.upisimulator.util.DataStore;
import com.upisimulator.exception.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class PaymentService {
    public static final BigDecimal DAILY_LIMIT = new BigDecimal("10000.00");
    private final DataStore store;

    public PaymentService(DataStore store) { this.store = store; }

    public synchronized UpiUser register(String name, String upi, String pin, BigDecimal opening)
            throws Exception {
        name = name.trim(); upi = upi.trim().toLowerCase();
        if (name.isEmpty()) throw new IllegalArgumentException("Name cannot be empty.");
        if (!upi.matches("[a-z0-9._-]{3,}@[a-z]{2,}"))
            throw new IllegalArgumentException("UPI ID must look like name@bank.");
        if (store.getUsers().containsKey(upi)) throw new IllegalArgumentException("UPI ID already exists.");
        if (pin == null || !pin.matches("\\d{4,6}")) throw new IllegalArgumentException("PIN must contain 4 to 6 digits.");
        validateAmount(opening);
        if (opening.compareTo(new BigDecimal("1000000")) > 0)
            throw new InvalidAmountException("Opening balance cannot exceed Rs. 10,00,000.");
        UpiUser user = new UpiUser(name, upi, pin, new BankAccount(upi, opening));
        store.getUsers().put(upi, user); store.save();
        return user;
    }

    public synchronized UpiUser login(String upi, String pin) throws Exception {
        UpiUser user = store.getUsers().get(upi.trim().toLowerCase());
        if (user == null) throw new UserNotFoundException("UPI ID is not registered.");
        if (user.isLocked()) throw new AccountLockedException("Account locked after three incorrect PIN attempts.");
        if (!user.getPin().equals(pin)) {
            user.recordFailedPinAttempt(); store.save();
            if (user.isLocked()) throw new AccountLockedException("Three incorrect attempts. Account locked.");
            throw new InvalidUpiPinException("Incorrect PIN. Attempts used: " + user.getFailedPinAttempts() + "/3");
        }
        user.resetFailedPinAttempts(); store.save();
        return user;
    }

    public synchronized PaymentRecord sendMoney(String senderUpi, String receiverUpi,
            String pin, BigDecimal amount) throws Exception {
        String from = senderUpi.trim().toLowerCase(), to = receiverUpi.trim().toLowerCase();
        UpiUser sender = store.getUsers().get(from), receiver = store.getUsers().get(to);
        if (sender == null || receiver == null) throw new UserNotFoundException("Sender or receiver UPI ID not found.");
        if (from.equals(to)) throw new IllegalArgumentException("You cannot transfer money to yourself.");
        if (sender.isLocked()) throw new AccountLockedException("Sender account is locked.");
        if (!sender.getPin().equals(pin)) {
            sender.recordFailedPinAttempt(); store.save();
            throw new InvalidUpiPinException(sender.isLocked() ? "Account locked after three incorrect PIN attempts." : "Incorrect UPI PIN.");
        }
        validateAmount(amount);
        BigDecimal todayTotal = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();
        for (PaymentRecord r : store.getTransactions()) {
            if (r.getSender().equals(from) && r.getType() == TransactionType.TRANSFER &&
                r.getStatus() == TransactionStatus.SUCCESS && r.getTime().toLocalDate().equals(today))
                todayTotal = todayTotal.add(r.getAmount());
        }
        if (todayTotal.add(amount).compareTo(DAILY_LIMIT) > 0)
            throw new DailyLimitExceededException("Daily outgoing limit of Rs. 10,000 exceeded.");
        if (sender.getAccount().getBalance().compareTo(amount) < 0)
            throw new InsufficientBalanceException("Insufficient balance.");

        // Atomic-style update: if receiver credit fails after debit, credit the sender back.
        sender.getAccount().debit(amount);
        try {
            receiver.getAccount().credit(amount);
        } catch (RuntimeException failure) {
            sender.getAccount().credit(amount); // reversal / rollback
            PaymentRecord failed = new PaymentRecord(from, to, amount, TransactionType.TRANSFER,
                    TransactionStatus.REVERSED, "Transfer failed; debit reversed.");
            store.getTransactions().add(failed); store.save();
            throw new IllegalStateException("Transfer failed. Debited amount was reversed.", failure);
        }
        PaymentRecord record = new PaymentRecord(from, to, amount, TransactionType.TRANSFER,
                TransactionStatus.SUCCESS, "Payment completed");
        store.getTransactions().add(record); store.save();
        return record;
    }

    public synchronized PaymentRequest requestMoney(String requester, String payer, BigDecimal amount) throws Exception {
        validateAmount(amount);
        requester = requester.trim().toLowerCase(); payer = payer.trim().toLowerCase();
        if (!store.getUsers().containsKey(requester) || !store.getUsers().containsKey(payer))
            throw new UserNotFoundException("Both UPI IDs must be registered.");
        if (requester.equals(payer)) throw new IllegalArgumentException("Cannot request money from yourself.");
        PaymentRequest request = new PaymentRequest(requester, payer, amount);
        store.getRequests().add(request); store.save();
        return request;
    }

    public synchronized BigDecimal getBalance(String upi) throws UserNotFoundException {
        UpiUser user = store.getUsers().get(upi.toLowerCase());
        if (user == null) throw new UserNotFoundException("User not found.");
        return user.getAccount().getBalance();
    }

    public synchronized List<PaymentRecord> getTransactionsFor(String upi) {
        List<PaymentRecord> result = new ArrayList<>();
        Iterator<PaymentRecord> iterator = store.getTransactions().iterator();
        while (iterator.hasNext()) {
            PaymentRecord r = iterator.next();
            if (r.getSender().equalsIgnoreCase(upi) || r.getReceiver().equalsIgnoreCase(upi)) result.add(r);
        }
        Collections.reverse(result);
        return result;
    }

    public synchronized List<PaymentRecord> getMiniStatement(String upi) {
        List<PaymentRecord> all = getTransactionsFor(upi);
        return new ArrayList<>(all.subList(0, Math.min(5, all.size())));
    }

    public synchronized List<PaymentRequest> getRequestsFor(String upi) {
        List<PaymentRequest> result = new ArrayList<>();
        for (PaymentRequest r : store.getRequests())
            if (r.getRequesterUpi().equalsIgnoreCase(upi) || r.getRequestedFromUpi().equalsIgnoreCase(upi)) result.add(r);
        Collections.reverse(result);
        return result;
    }

    public synchronized List<String> getRegisteredUsers() {
        List<String> result = new ArrayList<>();
        for (UpiUser u : store.getUsers().values())
            result.add(u.getFullName() + " (" + u.getUpiId() + ") - Rs. " + u.getAccount().getBalance());
        return result;
    }

    private void validateAmount(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidAmountException("Amount must be greater than zero.");
        if (amount.scale() > 2) throw new InvalidAmountException("Amount can have at most two decimal places.");
    }
}