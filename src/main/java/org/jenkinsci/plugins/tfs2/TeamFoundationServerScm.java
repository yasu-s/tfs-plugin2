package org.jenkinsci.plugins.tfs2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.jenkinsci.plugins.tfs2.browsers.TeamFoundationServerRepositoryBrowser;
import org.jenkinsci.plugins.tfs2.service.TFSService;
import org.jenkinsci.plugins.tfs2.util.ChangeSetFileUtil;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
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
    private final String projectCollection;
    private final String userName;
    private final String userPassword;
    private ProjectLocation[] locations = new ProjectLocation[0];
    private final TeamFoundationServerRepositoryBrowser browser;

    @DataBoundConstructor
    public TeamFoundationServerScm(String serverUrl, String projectCollection, String userName, String userPassword, List<ProjectLocation> locations, TeamFoundationServerRepositoryBrowser browser) {
        this.serverUrl         = serverUrl;
        this.projectCollection = projectCollection;
        this.userName          = userName;
        this.userPassword      = userPassword;
        this.locations         = locations.toArray(new ProjectLocation[locations.size()]);
        this.browser           = browser;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getProjectCollection() {
        return projectCollection;
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

    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        Map<String, Integer> changeSets = ChangeSetFileUtil.parseChangeSetFile(build);
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
            return PollingResult.BUILD_NOW;
        }

        AbstractBuild<?, ?> lastCompletedBuild = project.getLastCompletedBuild();
        if (lastCompletedBuild != null) {
            Map<String, Integer> changeSets = getRemoteChangeSets();
            if (changeSets.size() != state.getChangeSets().size())
                return PollingResult.BUILD_NOW;

            for (Entry<String, Integer> entry : state.getChangeSets().entrySet()) {
                if (changeSets.containsKey(entry.getKey())) {
                    if (changeSets.get(entry.getKey()) != entry.getValue())
                        return PollingResult.BUILD_NOW;
                } else
                    return PollingResult.BUILD_NOW;
            }
        }

        return PollingResult.NO_CHANGES;
    }

    private Map<String, Integer> getRemoteChangeSets() {
        TFSService service = new TFSService();
        service.setNativeDirectory(getDescriptor().getNativeDirectory());
        service.setServerUrl(serverUrl);
        service.setUserName(userName);
        service.setUserPassword(userPassword);
        service.init();

        Map<String, Integer> changeSets = new HashMap<String, Integer>();
        for (ProjectLocation location : locations) {
            if (service.pathExists(location.getProjectPath())) {
                int changeSetID = service.getChangeSetID(location.getProjectPath());
                changeSets.put(location.getProjectPath(), changeSetID);
            }
        }
        return changeSets;
    }


    @Override
    public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        return false;
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

        private static final String PROPERTY_NAME_NATIVE_DIRECTORY = "com.microsoft.tfs.jni.native.base-directory";
        private String nativeDirectory = null;

        public DescriptorImpl() {
            super(TeamFoundationServerRepositoryBrowser.class);
            load();
        }

        @Override
        public SCM newInstance(StaplerRequest req, JSONObject jsonObject) throws FormException {
            return super.newInstance(req, jsonObject);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            nativeDirectory = Util.fixEmpty(req.getParameter("tfs2.nativeDirectory").trim());
            save();
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.TeamFoundationServerScm_Descriptor_DisplayName();
        }

        public String getNativeDirectory() {
            return nativeDirectory;
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
