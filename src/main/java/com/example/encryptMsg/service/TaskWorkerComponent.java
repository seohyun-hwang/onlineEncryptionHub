package com.example.encryptMsg.service;

import com.example.encryptMsg.service.TaskScheduleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "APP_MODE", havingValue = "worker")
public class TaskWorkerComponent implements CommandLineRunner {

    private final TaskScheduleService scheduler;
    private final String workerId = "node-" + UUID.randomUUID().toString().substring(0, 6);

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
                    System.out.println("Task " + task.id() + "claimed by worker " + workerId + ".");

                    Thread.sleep(300);
                    scheduler.completeTask(task.id(), "encrypted_" + task.payload().hashCode());
                    System.out.println("Task " + task.id() + " completed.");
                }
            } catch (Exception e) {
                System.out.println("Worker " + workerId + " failed to execute task.");
                System.err.print(e.getMessage());
            }
        }
    }
}