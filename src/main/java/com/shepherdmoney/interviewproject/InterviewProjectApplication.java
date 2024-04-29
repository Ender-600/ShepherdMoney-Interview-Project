package com.shepherdmoney.interviewproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class InterviewProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterviewProjectApplication.class, args);
    }

}
