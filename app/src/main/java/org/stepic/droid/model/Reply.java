package org.stepic.droid.model;

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
    private final List<String> blanks;

    public static class Builder {

        private List<Boolean> choices;
        private String text;
        private List<Attachment> attachments;
        private String formula;
        private String number;
        private List<Integer> ordering;
        private String language;
        private String code;
        private List<String> blanks;

        public Builder() {
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

        public Builder setBlanks(List<String> blanks) {
            this.blanks = blanks;
            return this;
        }

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
        blanks = builder.blanks;
    }

    public String getNumber() {
        return number;
    }

    public String getText() {
        return text;
    }

    public List<Boolean> getChoices() {
        return choices;
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

    public List<String> getBlanks() {
        return blanks;
    }
}
