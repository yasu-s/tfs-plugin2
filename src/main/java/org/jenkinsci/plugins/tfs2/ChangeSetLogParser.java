package org.jenkinsci.plugins.tfs2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.digester.Digester;
import org.jenkinsci.plugins.tfs2.model.LogEntry;
import org.jenkinsci.plugins.tfs2.model.Path;
import org.jenkinsci.plugins.tfs2.model.WorkItemID;
import org.jenkinsci.plugins.tfs2.model.TFSChangeLogSet;
import org.xml.sax.SAXException;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import hudson.util.Digester2;
import hudson.util.IOException2;

public class ChangeSetLogParser extends ChangeLogParser {

    @Override
    public TFSChangeLogSet parse(@SuppressWarnings("rawtypes") AbstractBuild build, File changelogFile) throws IOException, SAXException {
        Digester digester = new Digester2();
        ArrayList<LogEntry> r = new ArrayList<LogEntry>();
        digester.push(r);

        digester.addObjectCreate("*/logentry", LogEntry.class);
        digester.addSetProperties("*/logentry");
        digester.addBeanPropertySetter("*/logentry/author","user");
        digester.addBeanPropertySetter("*/logentry/date");
        digester.addBeanPropertySetter("*/logentry/msg");
        digester.addSetNext("*/logentry","add");

        digester.addObjectCreate("*/logentry/paths/path", Path.class);
        digester.addSetProperties("*/logentry/paths/path");
        digester.addBeanPropertySetter("*/logentry/paths/path","value");
        digester.addSetNext("*/logentry/paths/path","addPath");

        digester.addObjectCreate("*/logentry/workitemids/workitemid", WorkItemID.class);
        digester.addBeanPropertySetter("*/logentry/workitemids/workitemid", "id");
        digester.addSetNext("*/logentry/workitemids/workitemid","addWorkItemID");

        try {
            digester.parse(changelogFile);
        } catch (IOException e) {
            throw new IOException2("Failed to parse "+changelogFile,e);
        } catch (SAXException e) {
            throw new IOException2("Failed to parse "+changelogFile,e);
        }

        return new TFSChangeLogSet(build, r);
    }

}
