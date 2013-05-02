package org.jenkinsci.plugins.tfs2.model;

import java.util.Collection;

import hudson.model.User;

public class ChangeSet extends hudson.scm.ChangeLogSet.Entry {

    @Override
    public String getMsg() {
        return null;
    }

    @Override
    public User getAuthor() {
        return null;
    }

    @Override
    public Collection<String> getAffectedPaths() {
        return null;
    }
}
