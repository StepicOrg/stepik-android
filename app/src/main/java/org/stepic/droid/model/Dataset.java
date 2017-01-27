package org.stepic.droid.model;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Dataset {
    private boolean is_multiple_choice;
    private List<String> options;
    private String someStringValueFromServer;
    private List<Pair> pairs;
    private Boolean is_html_enabled;
    private List<FillBlankComponent> components;
    private List<String> rows;
    private List<String> columns;
    private String description;
    private boolean is_checkbox;

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

    @Nullable
    public List<Pair> getPairs() {
        return pairs;
    }

    public Boolean getIs_html_enabled() {
        return is_html_enabled;
    }

    public List<FillBlankComponent> getFillBlankComponents() {
        return components;
    }

    public String getDescriptionTableQuiz() {
        return description;
    }

    public boolean isTableCheckbox() {
        return is_checkbox;
    }

    public List<String> getTableRows() {
        return rows;
    }

    public List<String> getTableColumns() {
        return columns;
    }
}
