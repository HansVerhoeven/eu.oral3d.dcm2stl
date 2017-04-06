package com.dimension4.dcm2stl.service;

import com.dimension4.dcm2stl.model.Input;
import com.dimension4.dcm2stl.model.Task;
import com.dimension4.dcm2stl.model.TaskDTO;
import com.dimension4.dcm2stl.model.TaskStatus;
import com.dimension4.dcm2stl.model.TaskType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

public class InMemoryTaskRepository implements ITaskRepository {

    private final ConcurrentMap<String, Task> tasks = new ConcurrentHashMap<>();

    @Autowired
    private TaskQueue queue;
    
    @PostConstruct
    private void init() {
//        Task t1 = new Task();
//        
//        List<String> imagest1 = new ArrayList<>();
//        imagest1.add("https://s3-us-west-2.amazonaws.com/com-layeredprints-dcm2stl/00000000/IM-0001-0001.dcm");
//        imagest1.add("https://s3-us-west-2.amazonaws.com/com-layeredprints-dcm2stl/00000000/IM-0001-0002.dcm");
//        imagest1.add("https://s3-us-west-2.amazonaws.com/com-layeredprints-dcm2stl/00000000/IM-0001-0003.dcm");
//        imagest1.add("https://s3-us-west-2.amazonaws.com/com-layeredprints-dcm2stl/00000000/IM-0001-0004.dcm");
//        imagest1.add("https://s3-us-west-2.amazonaws.com/com-layeredprints-dcm2stl/00000000/IM-0001-0005.dcm");
//        
//        Input t1i1 = new Input(imagest1);
//        
//        t1.setInput(t1i1);
//        t1.setStatus(TaskStatus.QUEUED);
//        t1.setType(TaskType.CONVERT);
//        
//        this.tasks.put(t1.getId(), t1);
//        
//        this.queue.add(t1.getId());
    }
    
    @Override
    public Iterable<Task> findAll() {
        return this.tasks.values();
    }

    @Override
    public Task save(Task task) {
        String id = task.getId();
        this.tasks.put(id, task);
        return task;
    }
    
    @Override
    public Task create(TaskDTO taskDto) {
//        Task task = new Task();
        Task task = new Task(taskDto.getTaskId());
        Input input = new Input();
        input.setImage(taskDto.getInput().getImages());
        input.setCropRegion(taskDto.getInput().getCropRegion());
//        input.setTopRegion(taskDto.getInput().getTopRegion());
//        input.setBottomRegion(taskDto.getInput().getBottomRegion());
        task.setInput(input);
        task.setAwsOwnerId(taskDto.getAwsOwnerId());
        task.setStatus(TaskStatus.QUEUED);
        task.setType(TaskType.forName(taskDto.getType()));
        this.tasks.put(task.getId(), task);
        
        return task;
    }

    @Override
    public Task find(String id) {
        return this.tasks.get(id);
    }

    @Override
    public Task delete(String id) {
        Task found = this.tasks.get(id);
        if (found != null) {
            this.tasks.remove(id);
        }
        return found;
    }

}
