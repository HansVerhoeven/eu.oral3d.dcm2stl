/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dimension4.dcm2stl.main;

import com.dimension4.dcm2stl.service.ITaskRepository;
import com.dimension4.dcm2stl.service.InMemoryTaskRepository;
import com.dimension4.dcm2stl.service.TaskQueue;
import com.dimension4.dcm2stl.service.TaskQueuePoller;
import com.dimension4.dcm2stl.service.TaskResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * @author Brecht
 */
@SpringBootApplication
@Configuration
@EnableScheduling
@ComponentScan({"com.dimension4.dcm2stl.service", "com.dimension4.dcm2stl.controller", "com.dimension4.dcm2stl.model"})
public class DCM2STLServiceApplication {
    
    @Bean
    @Autowired
    public TaskResourceAssembler TaskResourceAssembler() {
        return new TaskResourceAssembler();
    }
    
    @Bean
    @Autowired
    public ITaskRepository ITaskRepository() {
        return new InMemoryTaskRepository();
    }
    
    @Bean
    @Autowired
    public TaskQueue TaskQueue() {
        return new TaskQueue();
    }
    
    @Bean
    @Autowired
    public TaskQueuePoller TaskQueuePoller() {
        return new TaskQueuePoller();
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(DCM2STLServiceApplication.class, args);
    }
}
