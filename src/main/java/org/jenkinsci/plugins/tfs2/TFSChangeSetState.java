package org.jenkinsci.plugins.tfs2;

import hudson.scm.SCMRevisionState;

import java.io.Serializable;
import java.util.Map;

public class TFSChangeSetState extends SCMRevisionState implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Map<String, Integer> changeSets;

    public TFSChangeSetState(Map<String, Integer> changeSets) {
        this.changeSets = changeSets;
    }

    public Map<String, Integer> getChangeSets() {
        return changeSets;
    }
}
