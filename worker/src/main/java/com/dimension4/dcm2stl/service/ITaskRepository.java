package com.dimension4.dcm2stl.service;

import com.dimension4.dcm2stl.model.Task;
import com.dimension4.dcm2stl.model.TaskDTO;

public interface ITaskRepository {

    Iterable<Task> findAll();

    Task save(Task task);
    
    Task create(TaskDTO taskDto);

    Task find(String id);

    Task delete(String id);
}
