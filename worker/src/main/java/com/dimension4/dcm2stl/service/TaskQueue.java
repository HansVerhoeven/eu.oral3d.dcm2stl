package com.dimension4.dcm2stl.service;

import com.dimension4.dcm2stl.model.Task;
import java.util.ArrayDeque;
import java.util.Queue;
import org.springframework.stereotype.Service;

/**
 *
 * @author Brecht
 */
public class TaskQueue {
    
    private Queue<String> queue = new ArrayDeque<>();
    
    public boolean peek() {
        return (this.queue.peek() != null);
    }
    
    public String poll() {
        return this.queue.poll();
    }
    
    public boolean add(String id) {
        return this.queue.offer(id);
    }

    public boolean remove(Task task) {
        return this.queue.remove(task.getId());
    }
}
