package org.jenkinsci.plugins.tfs2.model;

import org.jenkinsci.plugins.tfs2.util.Constants;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.scm.EditType;
import hudson.scm.ChangeLogSet.AffectedFile;

@ExportedBean
public class Path implements AffectedFile {

    private char action;
    private String value;

    public void setAction(String action) {
        this.action = action.charAt(0);
    }

    @Exported(name="file")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPath() {
        return getValue();
    }

    public EditType getEditType() {
        if (action == Constants.EDIT_ADD_CHAR)
            return EditType.ADD;
        else if (action == Constants.EDIT_DELETE_CHAR)
            return EditType.DELETE;
        else
            return null;
    }

}
