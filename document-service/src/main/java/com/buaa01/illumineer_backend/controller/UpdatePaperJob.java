package com.buaa01.illumineer_backend.controller;

import com.buaa01.illumineer_backend.service.StormService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UpdatePaperJob implements Job {

    @Autowired
    private StormService stormService;

    @Override
    public void execute(JobExecutionContext context) {
        CompletableFuture<String> future = stormService.getStorm();
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                System.out.println("Exception in scheduled task: " + exception.getMessage());
            } else {
                System.out.println("Scheduled task completed successfully with result: " + result);
            }
        });
    }
}