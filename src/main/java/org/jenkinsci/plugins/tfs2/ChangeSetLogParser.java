package org.jenkinsci.plugins.tfs2;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;

public class ChangeSetLogParser extends ChangeLogParser {

    @Override
    public TFSChangeLogSet parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
        return null;
    }

}
