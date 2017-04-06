package com.dimension4.dcm2stl.model;

import java.util.Arrays;

public enum TaskStatus {

    QUEUED(1, "queued"),
    PROCESSING(2, "processing"),
    READY(3, "ready");

    private int id;
    private String name;

    TaskStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static TaskStatus forId(int id) {
        return Arrays.stream(values())
                .filter(p -> p.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Task Status with id " + id));
    }

    public static TaskStatus forName(String name) {
        return Arrays.stream(values())
                .filter(p -> p.getName().toLowerCase().equals(name.toLowerCase())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Task Status with name " + name));
    }
}
