package org.jenkinsci.plugins.tfs2.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.tfs2.TeamFoundationServerScm;
import org.jenkinsci.plugins.tfs2.util.TFSUtil;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.model.AbstractBuild;

public class TFSChangeLogSet extends hudson.scm.ChangeLogSet<LogEntry> {

    private Map<String, Integer> changeSetMap;
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
        return changeSetMap.isEmpty();
    }

    public synchronized Map<String,Integer> getChangeSetMap() throws IOException {
        if (changeSetMap == null && build.getProject().getScm() instanceof TeamFoundationServerScm) {
            TeamFoundationServerScm scm = (TeamFoundationServerScm)build.getProject().getScm();
            changeSetMap = TFSUtil.parseChangeSetFile(scm.getChangeSetFile(build), scm.getLocations());
        }
        return changeSetMap;
    }

    @Exported
    public List<ChangeSetInfo> getChangeSets() throws IOException {
        List<ChangeSetInfo> r = new ArrayList<ChangeSetInfo>();
        for (Map.Entry<String, Integer> e : getChangeSetMap().entrySet())
            r.add(new ChangeSetInfo(e.getKey(), e.getValue()));
        return r;
    }

    @ExportedBean(defaultVisibility=999)
    public static final class ChangeSetInfo {
        @Exported public final String module;
        @Exported public final int changeSet;
        public ChangeSetInfo(String module, int changeSet) {
            this.module = module;
            this.changeSet = changeSet;
        }
    }

}
