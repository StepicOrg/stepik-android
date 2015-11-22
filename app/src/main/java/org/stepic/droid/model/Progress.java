package org.stepic.droid.model;

public class Progress {
    String id;
    String last_viewed;
    int score;
    int cost;
    int n_steps;
    int n_steps_passed;
    boolean is_passed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLast_viewed() {
        return last_viewed;
    }

    public void setLast_viewed(String last_viewed) {
        this.last_viewed = last_viewed;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getN_steps() {
        return n_steps;
    }

    public void setN_steps(int n_steps) {
        this.n_steps = n_steps;
    }

    public int getN_steps_passed() {
        return n_steps_passed;
    }

    public void setN_steps_passed(int n_steps_passed) {
        this.n_steps_passed = n_steps_passed;
    }

    public boolean is_passed() {
        return is_passed;
    }

    public void setIs_passed(boolean is_passed) {
        this.is_passed = is_passed;
    }
}
