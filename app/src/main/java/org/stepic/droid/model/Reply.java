package org.stepic.droid.model;

import java.util.ArrayList;
import java.util.List;

public class Reply {
    List<Boolean> choices;
    String text;
    List<Attachment> attachments;

    public Reply(String text) {
        this.text = text;
        attachments = new ArrayList<>();
    }

    public Reply(List<Boolean> choices) {
        this.choices = choices;
    }

    public String getText() {
        return text;
    }

    public List<Boolean> getChoices() {
        return choices;
    }
}
