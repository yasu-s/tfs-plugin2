package org.jenkinsci.plugins.tfs2.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import hudson.model.AbstractBuild;

public class TFSChangeLogSet extends hudson.scm.ChangeLogSet<LogEntry> {

    private final List<LogEntry> logs;

    public TFSChangeLogSet(AbstractBuild<?, ?> build, List<LogEntry> logs) {
        super(build);
        this.logs = Collections.unmodifiableList(logs);
    }

    public List<LogEntry> getLogs() {
        return logs;
    }

    public Iterator<LogEntry> iterator() {
        return logs.iterator();
    }

    @Override
    public boolean isEmptySet() {
        return false;
    }

}
