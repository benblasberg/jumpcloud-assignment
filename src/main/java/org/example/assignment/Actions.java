package org.example.assignment;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Thread-safe class for adding actions and retrieving stats.
 */
public class Actions {
    private final ConcurrentMap<String, Action> actionsByName;
    private final Gson gson;

    /**
     * Constructs a new {@link Actions} instance.
     */
    public Actions() {
        gson = new GsonBuilder().create();
        actionsByName = new ConcurrentHashMap<>();
    }

    /**
     * Adds the action represented by the given {@code json} string to the set of all actions.
     * @param json the json string representing the action. Cannot be null, blank, or empty.
     *
     * <p>Must contain the following fields:</p>
     * <ul>
     *   <li>{@code action} - a string representing the action being taken. Cannot be null, blank, or empty.</li>
     *   <li>{@code time} - an integer representing the amount of time the action took. Must be positive.</li>
     * </ul>
     *
     * @throws IllegalArgumentException if the given {@code json} is invalid
     */
    public void addAction(String json) {
        if (isNullBlankOrEmpty( json )) {
            throw new IllegalArgumentException("json cannot be null blank or empty, but was: " + json);
        }

        final AddActionJson action = gson.fromJson( json, AddActionJson.class );

        if (isNullBlankOrEmpty( action.getAction() )) {
            throw new IllegalArgumentException("action cannot be null blank or empty, but was: " + action.getAction());
        }

        if (action.getTime() <= 0) {
            throw new IllegalArgumentException("time must be positive, but was: " + action.getTime());
        }

        //atomically add or update the action in the concurrent map
        actionsByName.compute(action.action, (key, val) -> {
            Action a = Optional.ofNullable(val).orElse( new Action( key ) );
            a.addTime( action.time );
            return a;
        } );
    }

    /**
     * Builds and returns a list of the average time taken for each action
     * @return a {@link String} representing the json equivalent of a list of actions with their average times, or an empty list if no actions have been added.
     *
     * <p>Example:</p>
     * <pre>
     *     [
     *         { "action": "run", "avg": 200 },
     *         { "action": "walk", "avg": 500 }
     *     ]
     * </pre>
     */
    public String getStats() {
            final List<ActionAverageJson> actionAverageJsons = actionsByName
                .entrySet()
                .stream()
                .map( entry -> new ActionAverageJson( entry.getKey(), entry.getValue().getAvg() ) )
                .collect( Collectors.toList() );
            return gson.toJson( actionAverageJsons );
    }

    private boolean isNullBlankOrEmpty(final String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Java class representation of the json used to add an action
     */
    private static class AddActionJson {
        private String action;
        private int time;

        public String getAction() {
            return action;
        }

        public int getTime() {
            return time;
        }
    }

    /**
     * Java class representation of the json used to return action averages
     */
    private static class ActionAverageJson {
        private String action;
        private int avg;

        public ActionAverageJson(String action, int avg) {
            this.action = action;
            this.avg = avg;
        }
    }
}
