package com.upisimulator.util;

public class AutosaveTask implements Runnable {
    private final DataStore store;
    public AutosaveTask(DataStore store) { this.store = store; }
    @Override public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try { Thread.sleep(30000); store.save(); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }
}