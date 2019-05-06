package com.slvinterface.json;

import com.slvinterface.enumeration.NotesType;

public class FormValues {

    public int id;
    private String label;
    private String value;
    private NotesType type;
    private int count;
    private int groupId = -1;
    private int groupRepeatableCount = -1;
    private boolean isGroup;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public NotesType getType() {
        return type;
    }

    public void setType(NotesType type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupRepeatableCount() {
        return groupRepeatableCount;
    }

    public void setGroupRepeatableCount(int groupRepeatableCount) {
        this.groupRepeatableCount = groupRepeatableCount;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }
}
