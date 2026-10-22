package com.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Task Manager backend.
 *
 * <p>Spring Boot scans the {@code com.taskmanager} package, so every component
 * (controllers, services, repositories...) is auto-detected from there.
 */
@SpringBootApplication
public class TaskManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
