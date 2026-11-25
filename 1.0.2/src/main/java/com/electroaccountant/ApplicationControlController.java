package com.electroaccountant;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Controls the locally installed application; it is only bound to 127.0.0.1. */
@RestController
@RequestMapping("/api/application")
public class ApplicationControlController {
    private final ConfigurableApplicationContext applicationContext;
    private final boolean desktopMode = Boolean.getBoolean("electro.accountant.desktop-mode");

    public ApplicationControlController(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping("/mode")
    public Map<String, Boolean> mode() {
        return Map.of("desktopMode", desktopMode);
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stop() {
        if (!desktopMode) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Thread shutdownThread = new Thread(() -> {
            try {
                // Let the HTTP response reach the browser before closing Tomcat.
                Thread.sleep(300);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            SpringApplication.exit(applicationContext, () -> 0);
        }, "electro-accountant-shutdown");
        shutdownThread.setDaemon(true);
        shutdownThread.start();
        return ResponseEntity.noContent().build();
    }
}
