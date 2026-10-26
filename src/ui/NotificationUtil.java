package ui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

public class NotificationUtil {
    
    // Displays an OS-level notification
    public static void showNotification(String title, String message, MessageType type) {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported. Message: " + title + " - " + message);
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            
            // Try to use a default Java icon
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png"); 
            
            TrayIcon trayIcon = new TrayIcon(image, "Study Group App");
            trayIcon.setImageAutoSize(true);
            
            tray.add(trayIcon);
            
            // Show the notification
            trayIcon.displayMessage(title, message, type);
            
            // Remove it after displaying so it doesn't clutter the tray
            // We use a separate thread so it doesn't block
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    tray.remove(trayIcon);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added.");
        }
    }
}
