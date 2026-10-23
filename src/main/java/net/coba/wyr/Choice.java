package net.coba.wyr;

/**
 * A class to hold a pair of choices (positive and negative) and a unique Identifier.
 */
public class Choice {
    private final String id;
    private final String positive;
    private final String negative;

    public Choice(String id, String positive, String negative) {
        this.id = id;
        this.positive = positive;
        this.negative = negative;
    }

    public String getId() {
        return id;
    }

    public String positive() {
        return positive;
    }

    public String negative() {
        return negative;
    }
}