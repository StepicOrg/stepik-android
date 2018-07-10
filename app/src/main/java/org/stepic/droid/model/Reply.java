package org.stepic.droid.model;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.stepik.android.model.learning.submissions.Attachment;

import java.util.List;

public class Reply {
    private final List<Boolean> choices;
    private final String text;
    private final List<Attachment> attachments;
    private final String formula;
    private final String number;
    private final List<Integer> ordering;
    private final String language;
    private final String code;

    @SerializedName("solve_sql")
    private final String solveSql;
    private final List<String> blanks;

    private List<TableChoiceAnswer> tableChoices; //this is not serialize by default, because  field 'choices' is already created by different type

    public static class Builder {
        private List<Boolean> choices;
        private String text;
        private List<Attachment> attachments;
        private String formula;
        private String number;
        private List<Integer> ordering;
        private String language;
        private String code;
        private String solveSql;
        private List<String> blanks;


        public List<TableChoiceAnswer> tableChoices;

        public Builder() {
        }

        public Builder setTableChoices(List<TableChoiceAnswer> tableChoices) {
            this.tableChoices = tableChoices;
            return this;
        }

        public Builder setChoices(List<Boolean> choices) {
            this.choices = choices;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setAttachments(List<Attachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        public Builder setFormula(String formula) {
            this.formula = formula;
            return this;
        }

        public Builder setNumber(String number) {
            this.number = number;
            return this;
        }

        public Builder setOrdering(List<Integer> ordering) {
            this.ordering = ordering;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setSolveSql(String solveSql) {
            this.solveSql = solveSql;
            return this;
        }

        public Builder setBlanks(List<String> blanks) {
            this.blanks = blanks;
            return this;
        }

        @NotNull
        public Reply build() {
            return new Reply(this);
        }

    }

    private Reply(Builder builder) {
        choices = builder.choices;
        text = builder.text;
        attachments = builder.attachments;
        formula = builder.formula;
        number = builder.number;
        ordering = builder.ordering;
        language = builder.language;
        code = builder.code;
        solveSql = builder.solveSql;
        blanks = builder.blanks;
        tableChoices = builder.tableChoices;
    }

    public String getNumber() {
        return number;
    }

    public String getText() {
        return text;
    }

    public List<Boolean> getChoices() {
        return (List<Boolean>) choices;
    }

    public String getFormula() {
        return formula;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public List<Integer> getOrdering() {
        return ordering;
    }

    public String getLanguage() {
        return language;
    }

    public String getCode() {
        return code;
    }

    public String getSolveSql() {
        return solveSql;
    }

    public List<String> getBlanks() {
        return blanks;
    }

    public List<TableChoiceAnswer> getTableChoices() {
        return tableChoices;
    }

    public void setTableChoices(List<TableChoiceAnswer> tableChoices) {
        this.tableChoices = tableChoices;
    }
}
