package org.jenkinsci.plugins.tfs2.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.export.Exported;

import hudson.model.User;

public class LogEntry extends hudson.scm.ChangeLogSet.Entry {

    private int changeSetID;
    private User author;
    private String date;
    private String msg;
    private List<Path> paths = new ArrayList<Path>();

    public int getChangeSetID() {
        return changeSetID;
    }

    public void setChangeSetID(int changeSetID) {
        this.changeSetID = changeSetID;
    }

    @Override
    public String getCommitId() {
        return String.valueOf(changeSetID);
    }

    @Override
    public long getTimestamp() {
        // TODO:
        return date != null ? 0 : -1;
    }

    @Override
    public User getAuthor() {
        if (author == null)
            return User.getUnknown();
        else
            return author;
    }

    public void setUser(String author) {
        this.author = User.get(author);
    }

    @Exported
    public String getUser() {
        return author != null ? author.getDisplayName() : "unknown";
    }

    @Exported
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override @Exported
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void addPath(Path p) {
        paths.add(p);
    }

    @Exported
    public List<Path> getPaths() {
        return paths;
    }

    @Override
    public Collection<Path> getAffectedFiles() {
        return paths;
    }

    @Override
    public Collection<String> getAffectedPaths() {
        List<String> affectedPaths = new ArrayList<String>(paths.size());
        for (Path path : paths) {
            affectedPaths.add(path.getPath());
        }
        return affectedPaths;
    }
}
