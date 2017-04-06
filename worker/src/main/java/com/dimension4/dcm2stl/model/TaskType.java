package com.dimension4.dcm2stl.model;


import java.util.Arrays;

public enum TaskType {

    CONVERT(1, "convert");

    private int id;
    private String name;

    TaskType(int id, String name) {
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

    public static TaskType forId(int id) {
        return Arrays.stream(values())
                .filter(p -> p.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Task Type with id " + id));
    }

    public static TaskType forName(String name) {
        return Arrays.stream(values())
                .filter(p -> p.getName().toLowerCase().equals(name.toLowerCase())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Task Type with name " + name));
    }
}
