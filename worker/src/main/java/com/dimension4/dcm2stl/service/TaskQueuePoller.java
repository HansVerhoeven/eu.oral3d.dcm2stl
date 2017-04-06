package com.dimension4.dcm2stl.service;

import com.dimension4.dcm2stl.model.Task;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Brecht
 */
@Service
public class TaskQueuePoller {
    
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(S3BucketService.class.getName());
    
    @Autowired
    private TaskExecutor executor;
    
    @Autowired
    private TaskQueue queue;

    @Autowired
    private ITaskRepository repository;
    
    public TaskQueuePoller() {
    }
    
    @Scheduled(fixedRate = 20000)
    private void pollAndExecute() {
//        LOGGER.log(Level.INFO, "Checking task queue...");
        if(this.queue.peek()) {
            String taskId = this.queue.poll();
            LOGGER.log(Level.INFO, "New task [{0}] found, attempting execution...", taskId);
            this.executor.execute(taskId);
            LOGGER.log(Level.INFO, "Executed task [{0}]", taskId);
        } else {
            LOGGER.log(Level.INFO, "No new tasks");
        }
//        LOGGER.log(Level.INFO, "Done checking task queue");
        
//        LOGGER.log(Level.INFO, "Checking db");
        Iterable<Task> tasks = this.repository.findAll();
        for(Task t:tasks) {
            LOGGER.log(Level.INFO, "task {0} has status {1}", new Object[]{t.getId(), t.getStatus().getName()});
        }
    }
    
   
}
