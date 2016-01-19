package org.stepic.droid.model;

import java.util.List;

public class Reply {
    List<Boolean> choices;
    String text;

    public Reply(String text) {
        this.text = text;
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
