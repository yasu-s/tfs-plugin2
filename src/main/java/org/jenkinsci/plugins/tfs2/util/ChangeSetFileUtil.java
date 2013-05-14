package org.jenkinsci.plugins.tfs2.util;

import hudson.model.AbstractBuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class ChangeSetFileUtil {

    public static File getChangeSetFile(AbstractBuild<?, ?> build) {
        return new File(build.getRootDir(), "changeSet.txt");
    }

    public static Map<String, Integer> parseChangeSetFile(AbstractBuild<?, ?> build) throws IOException {
        Map<String, Integer> changeSets = new HashMap<String, Integer>();
        File file = getChangeSetFile(build);
        if (!file.exists())
            return changeSets;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                int index = line.lastIndexOf('/');

                if (index < 0)
                    continue;

                try {
                    String path = line.substring(0, index);
                    int changeSetID = Integer.parseInt(line.substring(index + 1));
                    changeSets.put(path, changeSetID);
                } catch (NumberFormatException ex) {
                }
            }
        } finally {
            if (br != null) br.close();
        }

        return changeSets;
    }

    public static void saveChangeSetFile(AbstractBuild<?, ?> build, Map<String, Integer> changeSets) throws IOException, InterruptedException  {
        PrintWriter w = null;
        try {
            w = new PrintWriter(new FileOutputStream(getChangeSetFile(build)));
            for (Entry<String, Integer> entry : changeSets.entrySet()) {
                w.println(entry.getKey() + "/" + entry.getValue());
            }
        } finally {
            if (w != null) w.close();
        }
    }
}
