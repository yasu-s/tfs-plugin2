package org.jenkinsci.plugins.tfs2.model;

public class WorkItemID {

    private int id;

    public WorkItemID() {

    }

    public WorkItemID(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
