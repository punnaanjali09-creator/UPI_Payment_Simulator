package com.upisimulator.util;

import com.upisimulator.model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DataStore {
    private final String dataPath;
    private HashMap<String, UpiUser> users = new HashMap<>();
    private ArrayList<PaymentRecord> transactions = new ArrayList<>();
    private ArrayList<PaymentRequest> requests = new ArrayList<>();

    public DataStore(String dataPath) { this.dataPath = dataPath; load(); }
    @SuppressWarnings("unchecked")
    public synchronized void load() {
        Path path = Paths.get(dataPath);
        if (!Files.exists(path)) return;
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            users = (HashMap<String, UpiUser>) in.readObject();
            transactions = (ArrayList<PaymentRecord>) in.readObject();
            requests = (ArrayList<PaymentRequest>) in.readObject();
        } catch (Exception ex) {
            System.err.println("Could not load saved data: " + ex.getMessage());
        }
    }
    public synchronized void save() {
        try {
            Path path = Paths.get(dataPath);
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
                out.writeObject(users); out.writeObject(transactions); out.writeObject(requests);
            }
        } catch (IOException ex) {
            System.err.println("Could not save data: " + ex.getMessage());
        }
    }
    public HashMap<String, UpiUser> getUsers() { return users; }
    public ArrayList<PaymentRecord> getTransactions() { return transactions; }
    public ArrayList<PaymentRequest> getRequests() { return requests; }
}