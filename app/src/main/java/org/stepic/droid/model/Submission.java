package org.stepic.droid.model;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

public class Submission {

    public enum Status {

        @SerializedName("correct")
        CORRECT("correct"),

        @SerializedName("wrong")
        WRONG("wrong"),

        @SerializedName("evaluation")
        EVALUATION("evaluation"),

        @SerializedName("local")
        LOCAL("local");


        private final String scope;

        public String getScope() {
            return scope;
        }

        Status(String value) {
            this.scope = value;
        }

    }

    private long id;
    @Nullable
    private Status status;
    private String score;
    private String hint;
    private String time;
    private Reply reply;
    private long attempt;
    private String session;
    private String eta;

    public Submission(Reply reply, long attempt) {
        this.reply = reply;
        this.attempt = attempt;
    }

    public Submission(Reply reply, long attempt, Status status) {
        this(reply, attempt);
        this.status = status;
    }

    public long getId() {
        return id;
    }

    @Nullable
    public Status getStatus() {
        return status;
    }

    public String getScore() {
        return score;
    }

    public String getHint() {
        return hint;
    }

    public String getTime() {
        return time;
    }

    public Reply getReply() {
        return reply;
    }

    public long getAttempt() {
        return attempt;
    }

    public String getSession() {
        return session;
    }

    public String getEta() {
        return eta;
    }
}
