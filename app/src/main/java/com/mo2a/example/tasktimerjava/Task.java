package com.mo2a.example.tasktimerjava;

import java.io.Serializable;

class Task implements Serializable {
    public static final long serialVersionUID = 1;
    private long id;
    private final String name;
    private final String description;
    private final int sortOrder;

    public Task(long id, String name, String description, int sortOrder) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    int getSortOrder() {
        return sortOrder;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sortOrder=" + sortOrder +
                '}';
    }
}
