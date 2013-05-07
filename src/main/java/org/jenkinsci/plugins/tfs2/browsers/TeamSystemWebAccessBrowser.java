package org.jenkinsci.plugins.tfs2.browsers;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;

import org.jenkinsci.plugins.tfs2.Messages;
import org.jenkinsci.plugins.tfs2.model.ChangeSet;
import hudson.scm.RepositoryBrowser;

import java.io.IOException;
import java.net.URL;

import org.kohsuke.stapler.DataBoundConstructor;

public class TeamSystemWebAccessBrowser extends TeamFoundationServerRepositoryBrowser {

    private static final long serialVersionUID = 1L;

    private final String url;

    @DataBoundConstructor
    public TeamSystemWebAccessBrowser(String url) {
        this.url = Util.fixEmpty(url);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public URL getChangeSetLink(ChangeSet changeSet) throws IOException {
        return null;
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
