package org.jenkinsci.plugins.tfs2.service;

import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ChangeType;

public class ChangeItem {

    private String path;
    private ChangeType changeType = ChangeType.NONE;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }
}
