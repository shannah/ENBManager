package io.github.palexdev.enbmanager;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringEntry {
    public static void main(String[] args) {
        Application.launch(ENBManager.class, args);
    }
}