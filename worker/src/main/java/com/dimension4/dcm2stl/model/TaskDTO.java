package com.dimension4.dcm2stl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Data transfer object for Task entity
 * Created by Polina Petrenko on 01.12.2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDTO {

    @JsonProperty("id")
    private String taskId;
    // Type of task action
    @NotBlank
    private String type;
    // Name for Output format
    // Status of this task
    private String awsOwnerId;
    private String status;
    @NotNull
    @Valid
    private InputDTO input;
    
    private String outputUrl;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public InputDTO getInput() {
        return input;
    }

    public void setInput(InputDTO input) {
        this.input = input;
    }

    public String getOutputUrl() {
        return outputUrl;
    }

    public void setOutputUrl(String outputUrl) {
        this.outputUrl = outputUrl;
    }

    public String getAwsOwnerId() {
        return awsOwnerId;
    }

    public void setAwsOwnerId(String awsOwnerId) {
        this.awsOwnerId = awsOwnerId;
    }
    
    
}
