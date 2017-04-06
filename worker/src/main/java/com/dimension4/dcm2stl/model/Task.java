package com.dimension4.dcm2stl.model;

import com.dimension4.dcm2stl.service.Util;

public class Task {

    private String id;
    private TaskType type;
    private TaskStatus status;
    private Input input;
    private String outputURL;
    private String awsOwnerId;
    
    public Task() {
        this.id = Util.generateId();
    }
    
    public Task(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public String getOutputURL() {
        return outputURL;
    }

    public void setOutputURL(String outputURL) {
        this.outputURL = outputURL;
    }

    public String getAwsOwnerId() {
        return awsOwnerId;
    }

    public void setAwsOwnerId(String awsOwnerId) {
        this.awsOwnerId = awsOwnerId;
    }
}
