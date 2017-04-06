package com.dimension4.dcm2stl.controller;

import com.dimension4.dcm2stl.model.Task;
import com.dimension4.dcm2stl.model.TaskDTO;
import com.dimension4.dcm2stl.service.ITaskRepository;
import com.dimension4.dcm2stl.service.TaskQueue;
import com.dimension4.dcm2stl.service.TaskResourceAssembler;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


/**
 * Rest controller for Task entity
 * Created by Polina Petrenko on 01.12.2016.
 */
@RestController
@EnableAutoConfiguration
@RequestMapping(value = "/v1/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskController {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(TaskController.class.getName());

    @Autowired
    private ITaskRepository taskRepository;
    @Autowired
    private TaskResourceAssembler taskResourceAssembler;
    @Autowired
    private TaskQueue queue;

//    @Transactional(readOnly = true)
    @RequestMapping(value = "/{taskId}", method = RequestMethod.GET)
    public ResponseEntity<TaskDTO> get(@PathVariable @NotBlank String taskId,
                                       @RequestHeader(name = "X-User-Role", required = false) String userRole) {

//        if (isNotAdmin(userRole)) {
//            throw new AccessDeniedException();
//        }
        LOGGER.log(Level.INFO, "received GET");
        LOGGER.log(Level.INFO, "for task {0}", taskId);
        Task task = taskRepository.find(taskId);
        return ResponseEntity.ok(taskResourceAssembler.toResource(task));
    }

//    @Transactional
    @RequestMapping(value = "/{taskId}", method = RequestMethod.DELETE)
    public ResponseEntity<TaskDTO> delete(@PathVariable @NotBlank String taskId,
                                          @RequestHeader(name = "X-User-Role", required = false) String userRole) {

//        if (isNotAdmin(userRole)) {
//            throw new AccessDeniedException();
//        }
        Task task = taskRepository.delete(taskId);
        queue.remove(task);
        return ResponseEntity.ok(taskResourceAssembler.toResource(task));
    }

    // Avoid transactional because of sending data to SQS
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<TaskDTO> create(@RequestBody @Valid TaskDTO taskDTO,
                                          @RequestHeader(name = "X-User-Role", required = false) String userRole) {

//        if (isNotAdmin(userRole)) {
//            throw new AccessDeniedException();
//        }
        Task task = taskRepository.create(taskDTO);
        queue.add(task.getId());
//        task = taskRepository.sendTask(task.getId());
        return new ResponseEntity<>(taskResourceAssembler.toResource(task), HttpStatus.CREATED);
    }

//    @Transactional(readOnly = true)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<TaskDTO>> listAllTasks(@RequestHeader(name = "X-User-Role", required = false) String userRole) {

//        if (isNotAdmin(userRole)) {
//            throw new AccessDeniedException();
//        }
//        Page<Task> pagedTasks = taskRepository.findAllTasks(pageable);
//
//        List<Link> links = new ArrayList<>();
//        Link taskLink = new Link(linkTo(TaskController.class) + "/{id}", "task");
//        links.add(taskLink);
//
//        PagedResources<TaskDTO> resource = pagedResourcesAssembler.toResource(pagedTasks, taskResourceAssembler);
//        resource.add(links);
        Iterable<Task> tasks = taskRepository.findAll();
        List<TaskDTO> taskDtos = new ArrayList<>();
        for(Task t:tasks) {
            taskDtos.add(taskResourceAssembler.toResource(t));
        }
        return ResponseEntity.ok(taskDtos);
    }

    private boolean isNotAdmin(String userRole) {
        return !"admin".equals(userRole);
    }
}
