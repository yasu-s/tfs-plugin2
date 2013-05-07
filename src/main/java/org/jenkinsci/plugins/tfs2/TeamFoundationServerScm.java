package org.jenkinsci.plugins.tfs2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import net.sf.json.JSONObject;

import org.jenkinsci.plugins.tfs2.browsers.TeamFoundationServerRepositoryBrowser;
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
    private final String userName;
    private final String userPassword;
    private ProjectLocation[] locations = new ProjectLocation[0];
    private final TeamFoundationServerRepositoryBrowser browser;

    @DataBoundConstructor
    public TeamFoundationServerScm(String serverUrl, String userName, String userPassword, List<ProjectLocation> locations, TeamFoundationServerRepositoryBrowser browser) {
        this.serverUrl    = serverUrl;
        this.userName     = userName;
        this.userPassword = userPassword;
        this.locations    = locations.toArray(new ProjectLocation[locations.size()]);
        this.browser      = browser;
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

    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        return new TFSChangeSetState();
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState baseline) throws IOException, InterruptedException {
        if (project.getLastBuild() == null)
            return PollingResult.BUILD_NOW;

        return PollingResult.NO_CHANGES;
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
