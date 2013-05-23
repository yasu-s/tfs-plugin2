package org.jenkinsci.plugins.tfs2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.tfs2.browsers.TeamFoundationServerRepositoryBrowser;
import org.jenkinsci.plugins.tfs2.model.LogEntry;
import org.jenkinsci.plugins.tfs2.service.TFSService;
import org.jenkinsci.plugins.tfs2.util.TFSUtil;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SCM;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;

public class TeamFoundationServerScm extends SCM {

    private final String serverUrl;
    private final String userName;
    private final String userPassword;
    private ProjectLocation[] locations = new ProjectLocation[0];
    private final TeamFoundationServerRepositoryBrowser browser;
    private final String excludedRegions;
    private final String includedRegions;

    @DataBoundConstructor
    public TeamFoundationServerScm(String serverUrl, String userName, String userPassword, List<ProjectLocation> locations,
                                    TeamFoundationServerRepositoryBrowser browser, String excludedRegions, String includedRegions) {
        this.serverUrl       = serverUrl;
        this.userName        = userName;
        this.userPassword    = userPassword;
        this.locations       = locations.toArray(new ProjectLocation[locations.size()]);
        this.browser         = browser;
        this.excludedRegions = excludedRegions;
        this.includedRegions = includedRegions;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public ProjectLocation[] getLocations() {
        return locations;
    }

    @Override
    public TeamFoundationServerRepositoryBrowser getBrowser() {
        return browser;
    }

    public String getExcludedRegions() {
        return excludedRegions;
    }

    public String[] getExcludedRegionsNormalized() {
        return StringUtils.isBlank(excludedRegions) ? null : excludedRegions.split("[\\r\\n]+");
    }

    public Pattern[] getExcludedRegionsPatterns() {
        String[] excluded = getExcludedRegionsNormalized();
        if (excluded != null) {
            Pattern[] patterns = new Pattern[excluded.length];

            int i = 0;
            for (String excludedRegion : excluded) {
                patterns[i++] = Pattern.compile(excludedRegion);
            }

            return patterns;
        }

        return new Pattern[0];
    }

    public String getIncludedRegions() {
        return includedRegions;
    }

    public String[] getIncludedRegionsNormalized() {
        return StringUtils.isBlank(includedRegions) ? null : includedRegions.split("[\\r\\n]+");
    }

    public Pattern[] getIncludedRegionsPatterns() {
        String[] included = getIncludedRegionsNormalized();
        if (included != null) {
            Pattern[] patterns = new Pattern[included.length];

            int i = 0;
            for (String includedRegion : included) {
                patterns[i++] = Pattern.compile(includedRegion);
            }

            return patterns;
        }

        return new Pattern[0];
    }

    public File getChangeSetFile(AbstractBuild<?, ?> build) {
        return new File(build.getRootDir(), "changeSet.txt");
    }

    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        Map<String, Integer> changeSets = TFSUtil.parseChangeSetFile(getChangeSetFile(build), locations);
        return new TFSChangeSetState(changeSets);
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState baseline) throws IOException, InterruptedException {
        final TFSChangeSetState state;
        if (baseline instanceof TFSChangeSetState)
            state = (TFSChangeSetState)baseline;
        else if (project.getLastBuild() != null)
            state = (TFSChangeSetState)calcRevisionsFromBuild(project.getLastBuild(), launcher, listener);
        else
            state = new TFSChangeSetState(new HashMap<String, Integer>());

        if (project.getLastBuild() == null) {
            listener.getLogger().println("No Last Build.");
            return PollingResult.BUILD_NOW;
        }

        AbstractBuild<?, ?> lastCompletedBuild = project.getLastCompletedBuild();
        if (lastCompletedBuild != null) {
            Map<String, Integer> changeSets = getRemoteChangeSets();
            if (changeSets.size() != state.getChangeSets().size()) {
                listener.getLogger().println("ChangeSets.size() change.");
                return PollingResult.BUILD_NOW;
            }

            for (Entry<String, Integer> entry : state.getChangeSets().entrySet()) {
                if (changeSets.containsKey(entry.getKey())) {
                    if (!changeSets.get(entry.getKey()).equals(entry.getValue())) {
                        listener.getLogger().println(String.format("ChangeSet %d -> %d", entry.getValue(), changeSets.get(entry.getKey())));
                        return PollingResult.BUILD_NOW;
                    }
                } else {
                    listener.getLogger().println("ChangeSets.size() change.");
                    return PollingResult.BUILD_NOW;
                }
            }
        }

        return PollingResult.NO_CHANGES;
    }

    private Map<String, Integer> getRemoteChangeSets() {
        TFSService service = null;
        Map<String, Integer> changeSets = null;
        try {
            service = new TFSService(serverUrl, userName, userPassword);

            changeSets = new HashMap<String, Integer>();
            for (ProjectLocation location : locations) {
                if (service.pathExists(location.getProjectPath())) {
                    int changeSetID = service.getChangeSetID(location.getProjectPath(), getExcludedRegionsPatterns(), getIncludedRegionsPatterns());
                    changeSets.put(location.getProjectPath(), changeSetID);
                }
            }
        } finally {
            if (service != null) service.close();
        }
        return changeSets;
    }


    @Override
    public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        Map<String, Integer> changeSets = getRemoteChangeSets();


        TFSUtil.saveChangeSetFile(getChangeSetFile(build), changeSets);

        saveChangeSetLog(build, changelogFile);

        return true;
    }

    private void saveChangeSetLog(AbstractBuild<?, ?> build, File changelogFile) throws IOException, InterruptedException {
        File previousChangeSetFile = getPreviousChangeSetFile(build);
        File currentChangeSetFile  = getChangeSetFile(build);
        int previousChangeSetID = (previousChangeSetFile != null) ? TFSUtil.getChangeSetID(previousChangeSetFile, locations) : 0;
        int currentChangeSetID  = (currentChangeSetFile != null) ? TFSUtil.getChangeSetID(currentChangeSetFile, locations) : 0;
        TFSService service = null;
        try {
            service = new TFSService(serverUrl, userName, userPassword);
            List<LogEntry> logEntrys = service.getLogEntrys(previousChangeSetID, currentChangeSetID);

            ChangeSetLogWriter writer = new ChangeSetLogWriter();
            writer.write(changelogFile, logEntrys);
        } finally {
            if (service != null) service.close();
        }
    }

    private File getPreviousChangeSetFile(AbstractBuild<?, ?> build) {
        AbstractBuild<?, ?> previousBuild = null;
        File previousFile = null;
        while ((previousBuild = build.getPreviousBuild()) != null) {
            if (getChangeSetFile(previousBuild).exists()) {
                previousFile = getChangeSetFile(previousBuild);
                break;
            }
        }
        return previousFile;
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new ChangeSetLogParser();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends SCMDescriptor<TeamFoundationServerScm> {

        public DescriptorImpl() {
            super(TeamFoundationServerRepositoryBrowser.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.TeamFoundationServerScm_Descriptor_DisplayName();
        }
    }

    @ExportedBean
    public static final class ProjectLocation implements Serializable {

        private static final long serialVersionUID = 1L;
        private final String projectPath;
        private final String localDirectory;

        @DataBoundConstructor
        public ProjectLocation(String projectPath, String localDirectory) {
            this.projectPath    = projectPath;
            this.localDirectory = localDirectory;
        }

        public String getProjectPath() {
            return projectPath;
        }

        public String getLocalDirectory() {
            return localDirectory;
        }
    }
}
