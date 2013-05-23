package org.jenkinsci.plugins.tfs2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.jenkinsci.plugins.tfs2.model.LogEntry;
import org.jenkinsci.plugins.tfs2.model.Path;
import org.jenkinsci.plugins.tfs2.model.WorkItemID;


public class ChangeSetLogWriter {

    public void write(File changelogFile, List<LogEntry> logEntrys) throws IOException, InterruptedException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileOutputStream(changelogFile));

            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<log>");

            for (LogEntry logEntry : logEntrys) {
                writeLogEntry(writer, logEntry);
            }

            writer.println("</log>");
            writer.flush();
        } finally {
            if (writer != null) writer.close();
        }
    }

    private void writeLogEntry(PrintWriter writer, LogEntry logEntry) {
        writer.println(String.format("\t<logentry changeSetID=\"%s\">", logEntry.getChangeSetID()));

        writer.println(String.format("\t\t<author>%s</author>", logEntry.getUser()));
        writer.println(String.format("\t\t<date>%d</date>", logEntry.getDate()));
        writer.println(String.format("\t\t<msg>%s</msg>", logEntry.getMsg()));

        writer.println("\t\t<workitemids>");
        for (WorkItemID id : logEntry.getWorkItemIDs()) {
            writer.println(String.format("\t\t\t<workitemid>%d</workitemid>", id.getId()));
        }
        writer.println("\t\t</workitemids>");

        writer.println("\t\t<paths>");
        for (Path path : logEntry.getPaths()) {
            writer.println(String.format("\t\t\t<path action=\"%s\">%s</path>", path.getAction(), path.getValue()));
        }
        writer.println("\t\t</paths>");

        writer.println("\t</logentry>");
    }

}
