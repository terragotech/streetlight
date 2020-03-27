package com.terragoedge.streetlight.json.model;

import java.util.ArrayList;
import java.util.List;

public class WorkflowConfig {
    private int action;
    private NewAction newAction;
    private ReplaceAction replaceAction;
    private List<Integer> removeAction = new ArrayList<>();

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public NewAction getNewAction() {
        return newAction;
    }

    public void setNewAction(NewAction newAction) {
        this.newAction = newAction;
    }

    public ReplaceAction getReplaceAction() {
        return replaceAction;
    }

    public void setReplaceAction(ReplaceAction replaceAction) {
        this.replaceAction = replaceAction;
    }

    public List<Integer> getRemoveAction() {
        return removeAction;
    }

    public void setRemoveAction(List<Integer> removeAction) {
        this.removeAction = removeAction;
    }

    @Override
    public String toString() {
        return "Workflow{" +
                "action=" + action +
                ", newAction=" + newAction +
                ", replaceAction=" + replaceAction +
                ", removeAction=" + removeAction +
                '}';
    }
}
