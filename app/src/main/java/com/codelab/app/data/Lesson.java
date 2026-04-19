package com.codelab.app.data;

public class Lesson {
    public final int index;
    public final String title;
    public final String subtitle;
    public final String starterCode;
    public final String task;
    public final String hint;
    public final String expectedOutput;
    public final int xpReward;

    /** transient — computed at runtime, not part of pack JSON */
    public Status status = Status.NOT_STARTED;

    public enum Status { NOT_STARTED, IN_PROGRESS, COMPLETED }

    public Lesson(int index, String title, String subtitle, String starterCode,
                  String task, String hint, String expectedOutput, int xpReward) {
        this.index = index;
        this.title = title;
        this.subtitle = subtitle;
        this.starterCode = starterCode;
        this.task = task;
        this.hint = hint;
        this.expectedOutput = expectedOutput;
        this.xpReward = xpReward;
    }
}
