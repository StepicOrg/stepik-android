package org.stepic.droid.model;

import java.util.List;

public class Dataset {
    private boolean is_multiple_choice;
    List<String> options;

    public boolean is_multiple_choice() {
        return is_multiple_choice;
    }

    public List<String> getOptions() {
        return options;
    }
}
