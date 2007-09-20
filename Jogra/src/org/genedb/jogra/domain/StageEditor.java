package org.genedb.jogra.domain;

import org.genedb.zfexpression.domain.Stage;

import java.beans.PropertyEditorSupport;

public class StageEditor extends PropertyEditorSupport {
    
    @Override
    public String getAsText() {
        if (getValue() == null) {
            return "";
        }
        return getValue().toString();
    }

    
    @Override
    public void setAsText(String number) throws IllegalArgumentException {
        Stage stage = Stage.valueOf(number);
        if (stage == null) {
            throw new IllegalArgumentException("Can't find stage by number '"+number+"'");
        }
        setValue(stage);
    }

}
