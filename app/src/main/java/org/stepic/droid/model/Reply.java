package org.stepic.droid.model;

import java.util.List;

public class Reply {
    List<Boolean> choices;

    public Reply(List<Boolean> choices) {
        this.choices = choices;
    }

    public List<Boolean> getChoices() {
        return choices;
    }
}
