package com.example.encryptMsg.service;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(name = "APP_MODE", havingValue = "worker")
public class TaskWorkerComponent implements CommandLineRunner {

    private final TaskScheduleService scheduler;
    private final String workerId = "node-" + UUID.randomUUID().toString().substring(0, 6);
    private final AtomicBoolean running = new AtomicBoolean(true);

    public TaskWorkerComponent(TaskScheduleService scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run(String... args) {
        System.out.println("Worker " + workerId + " initialized.");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                var taskOpt = scheduler.claimTask(workerId, 5);

                if (taskOpt.isPresent()) {
                    var task = taskOpt.get();
                    System.out.println("Task " + task.id() + " claimed by worker " + workerId + ".");

                    Thread.sleep(300);
                    scheduler.completeTask(task.id(), "encrypted_" + task.payload().hashCode());
                    System.out.println("Task " + task.id() + " completed.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Worker " + workerId + " interrupted; shutting down.");
            } catch (Exception e) {
                System.err.println("Worker " + workerId + " failed to execute task: " + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.out.println("Worker " + workerId + " interrupted; shutting down.");
                }
            }
        }
    }

    @PreDestroy
    public void onKubernetesTermination() {
        System.out.println("SIGTERM received. Stopping worker " + workerId + " gracefully...");
        this.running.set(false);
    }
}