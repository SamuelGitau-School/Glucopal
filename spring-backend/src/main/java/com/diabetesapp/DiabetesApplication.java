package com.diabetesapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DiabetesApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        // Only push dotenv entries as system props if they actually came from a file
        // On Railway, env vars are already in the OS environment — Spring reads those natively
        dotenv.entries().forEach(e -> {
            if (System.getenv(e.getKey()) == null) {
                System.setProperty(e.getKey(), e.getValue());
            }
        });

        SpringApplication.run(DiabetesApplication.class, args);
    }
}
