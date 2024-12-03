package com.buaa01.illumineer_backend.config;

import com.buaa01.illumineer_backend.controller.UpdatePaperJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail updatePaperJobDetail() {
        return JobBuilder.newJob(UpdatePaperJob.class)
                .withIdentity("updatePaperJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger updatePaperTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(updatePaperJobDetail())
                .withIdentity("updatePaperTrigger")
//                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?")) // 每1分钟触发一次
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 ? * MON"))
                .build();
    }
}
