package com.arnobx86.smarthomeexpensemanager;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Initialize database before launching the application
        DatabaseInitializer.initialize();
        Application.launch(HelloApplication.class, args);
    }
}
