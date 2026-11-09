package com.upisimulator;

import com.upisimulator.service.PaymentService;
import com.upisimulator.ui.LoginFrame;
import com.upisimulator.util.DataStore;
import com.upisimulator.util.AutosaveTask;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { }
        DataStore store = new DataStore("data/upi-data.ser");
        PaymentService service = new PaymentService(store);
        Thread autosave = new Thread(new AutosaveTask(store), "upi-autosave");
        autosave.setDaemon(true);
        autosave.start();
        SwingUtilities.invokeLater(() -> new LoginFrame(service).setVisible(true));
    }
}