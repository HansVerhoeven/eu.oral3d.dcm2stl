package com.dimension4.dcm2stl.service;
//
//import com.layeredprints.controllers.TaskController;
//import com.layeredprints.models.database.Input;
//import com.layeredprints.models.database.Task;
//import com.layeredprints.models.resources.ImageDTO;
//import com.layeredprints.models.resources.InputDTO;
//import com.layeredprints.models.resources.TaskDTO;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.hateoas.Link;
//import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import com.dimension4.dcm2stl.model.ImageDTO;
import com.dimension4.dcm2stl.model.Input;
import com.dimension4.dcm2stl.model.InputDTO;
import com.dimension4.dcm2stl.model.Task;
import com.dimension4.dcm2stl.model.TaskDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
//
//import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
//import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Converts Task Entity to resource
 * Created by Polina Petrenko on 01.12.2016.
 */
@Service
//public class TaskResourceAssembler extends ResourceAssemblerSupport<Task, TaskDTO> {
public class TaskResourceAssembler {

    public TaskResourceAssembler() {
//        super(TaskController.class, TaskDTO.class);
    }

//    @Override
    public TaskDTO toResource(Task task) {

        if(task == null) {
            return null;
        }

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskId(task.getId());
        if (task.getType() != null) {
            taskDTO.setType(task.getType().getName());
        }
        if (task.getOutputURL()!= null) {
            taskDTO.setOutputUrl(task.getOutputURL());
        }
        if (task.getStatus() != null) {
            taskDTO.setStatus(task.getStatus().getName());
        }
        Input input = task.getInput();
        if(input != null && input.getImages() != null) {
            List<ImageDTO> imageDTOs = input.getImages().stream().map(ImageDTO::new).collect(Collectors.toList());
            InputDTO inputDTO = new InputDTO(imageDTOs);
            taskDTO.setInput(inputDTO);
        }

//        ResponseEntity<TaskDTO> taskLink = methodOn(TaskController.class).get(task.getId(), "");
//        taskDTO.add(linkTo(taskLink).withSelfRel());

//        if(StringUtils.isNotBlank(task.getOutputURL())) {
//            Link outputLink = new Link(task.getOutputURL(), "output");
//            taskDTO.add(outputLink);
//        }

        return taskDTO;
    }
}
