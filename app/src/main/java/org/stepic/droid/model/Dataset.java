package org.stepic.droid.model;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Dataset {
    private boolean is_multiple_choice;
    private List<String> options;
    private String someStringValueFromServer;

    public Dataset() {
    }

    public Dataset(String someStringValueFromServer) {
        this.someStringValueFromServer = someStringValueFromServer;
    }

    public boolean is_multiple_choice() {
        return is_multiple_choice;
    }

    public List<String> getOptions() {
        return options;
    }

    @Nullable
    public String getSomeStringValueFromServer() {
        return someStringValueFromServer;
    }
}
