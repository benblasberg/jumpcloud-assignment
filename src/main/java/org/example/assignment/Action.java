package org.example.assignment;

/**
 * Class representing the state of an action. This class is declared package private and is not meant to be used by a consumer.
 */
class Action {
    private String name;
    private int totalTime;
    private int totalEntries;

    /**
     * Constructs a new {@link Action} object.
     * @param name the name of this action.
     */
    public Action(String name) {
        this.name = name;
        this.totalTime = 0;
        this.totalEntries = 0;
    }

    /**
     * @return The name of this action.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the average time for this action.
     */
    public int getAvg() {
        if (totalEntries == 0) {
            return 0;
        }

        return totalTime/totalEntries;
    }

    /**
     * Adds a time to this action. Adding times to an action can affect the value returned by {@link Action#getAvg()}
     * @param time the time to add to this action.
     */
    public void addTime(int time) {
        totalTime += time;
        totalEntries++;
    }
}
