package org.jenkinsci.plugins.tfs2.model;

import org.jenkinsci.plugins.tfs2.util.Constants;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.scm.EditType;
import hudson.scm.ChangeLogSet.AffectedFile;

@ExportedBean
public class Path implements AffectedFile {

    private String action;
    private String value;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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
        if (Constants.CHANGE_TYPE_ADD.equals(action))
            return EditType.ADD;
        else if (Constants.CHANGE_TYPE_DELETE.equals(action))
            return EditType.DELETE;
        else
            return EditType.EDIT;
    }

}
