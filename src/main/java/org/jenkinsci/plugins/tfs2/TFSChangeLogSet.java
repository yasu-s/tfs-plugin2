package org.jenkinsci.plugins.tfs2;

import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jenkinsci.plugins.tfs2.model.ChangeSet;

public class TFSChangeLogSet extends hudson.scm.ChangeLogSet<ChangeSet> {

    private final List<ChangeSet> changesets;

    public TFSChangeLogSet(AbstractBuild<?, ?> build, List<ChangeSet> changesets) {
        super(build);
        this.changesets = changesets;
//        for (ChangeSet changeset : changesets) {
//            changeset.setParent(this);
//        }
    }

    public TFSChangeLogSet(AbstractBuild<?, ?> build, ChangeSet[] changesetArray) {
        super(build);
        changesets = new ArrayList<ChangeSet>();
//        for (ChangeSet changeset : changesetArray) {
//            changeset.setParent(this);
//            changesets.add(changeset);
//        }
    }

    @Override
    public boolean isEmptySet() {
        return changesets.isEmpty();
    }

    public Iterator<ChangeSet> iterator() {
        return changesets.iterator();
    }
}
