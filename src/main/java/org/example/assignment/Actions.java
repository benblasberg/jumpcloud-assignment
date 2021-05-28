package org.example.assignment;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Actions {

    private final ConcurrentMap<String, Action> actionsByName;
    private final Gson gson;

    public Actions() {
        gson = new GsonBuilder().create();
        actionsByName = new ConcurrentHashMap<>();
    }

    /**
     *
     * @param json
     * @throws IllegalArgumentException if the given {@code json} is invalid
     */
    public void add(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("json cannot be null blank or empty, but was: " + json);
        }
        final ActionJson action = gson.fromJson( json, ActionJson.class );

        if (action.action == null || action.action.trim().isEmpty()) {
            throw new IllegalArgumentException("action cannot be null blank or empty, but was: " + action.action);
        }

        if (action.time <= 0) {
            throw new IllegalArgumentException("time must be positive, but was: " + action.time);
        }

        actionsByName.compute(action.action, (key, val) -> {
            Action a = Optional.ofNullable(val).orElse( new Action( key ) );
            a.addTime( action.time );
            return a;
        } );
    }

    public String getStats() {
            final List<ActionAvg> actionAverages = actionsByName
                .entrySet()
                .stream()
                .map( entry -> new ActionAvg( entry.getKey(), entry.getValue().getAvg() ) )
                .collect( Collectors.toList() );
            return gson.toJson( actionAverages );
    }

    //todo: change the naming of this
    static class ActionJson {

        private String action;

        private int time;
    }

    static class ActionAvg {
        private String action;
        private int avg;

        public ActionAvg(String action, int avg) {
            this.action = action;
            this.avg = avg;
        }
    }

    private static class Action {
        private String name;
        private int totalTime;
        private int totalEntries;

        public Action(String name) {
            this.name = name;
        }

        public int getAvg() {
            return totalTime/totalEntries;
        }

        public void addTime(int time) {
            totalTime += time;
            totalEntries++;
        }
    }

}
