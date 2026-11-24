package com.electroaccountant;

import java.awt.Desktop;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ElectroAccountantApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ElectroAccountantApplication.class);
        // The packaged Windows launcher uses Desktop.browse to open the local UI.
        application.setHeadless(false);
        application.run(args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openLocalApplication(@Value("${server.port:8080}") int port) {
        if (!Boolean.getBoolean("electro.accountant.open-browser")) {
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + port));
            }
        } catch (Exception exception) {
            // The app remains usable at the local address if Windows blocks a browser launch.
            System.err.println("Could not open the local browser: " + exception.getMessage());
        }
    }
}
