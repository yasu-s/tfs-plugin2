package org.jenkinsci.plugins.tfs2.browsers;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.tfs2.Messages;
import org.jenkinsci.plugins.tfs2.model.LogEntry;
import org.jenkinsci.plugins.tfs2.util.Constants;

import hudson.scm.RepositoryBrowser;

import java.io.IOException;
import java.net.URL;

import org.kohsuke.stapler.DataBoundConstructor;

public class TeamSystemWebAccessBrowser extends TeamFoundationServerRepositoryBrowser {

    private static final long serialVersionUID = 1L;

    private final String version;
    private final String serverUrl;
    private final String projectCollection;
    private final String project;

    @DataBoundConstructor
    public TeamSystemWebAccessBrowser(String version, String serverUrl, String projectCollection, String project) {
        this.serverUrl         = Util.fixEmpty(serverUrl);
        this.version           = StringUtils.isBlank(version) ? Constants.VERSION_2012_2 : version;
        this.projectCollection = projectCollection;
        this.project           = project;
    }

    public String getVersion() {
        return version;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getProjectCollection() {
        return projectCollection;
    }

    public String getProject() {
        return project;
    }

    @Override
    public URL getChangeSetLink(LogEntry changeSet) throws IOException {
        if (Constants.VERSION_2012_2.equals(version))
            return new URL(String.format("%1$s%2$s/%3$s/_versionControl/changeset#cs=%4$d", serverUrl, projectCollection, project, changeSet.getChangeSetID()));
        else
            return new URL(String.format("%1$s%2$s/%3$s/_versionControl/changeset/%4$d", serverUrl, projectCollection, project, changeSet.getChangeSetID()));
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {

        public DescriptorImpl() {
            super(TeamSystemWebAccessBrowser.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.TeamSystemWebAccessBrowser_Descriptor_DisplayName();
        }
    }
}
