package org.example.assignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class Actions {

    private AtomicReference<Map<String, Action>> actionsByName = new AtomicReference<>(new HashMap<>());

    /**
     *
     * @param json
     */
    public void add(String json) {
        //deserialize the json

        final ActionJson action = deserialize( json );

        synchronized ( this ) {
            final Action existingAction = actionsByName.get()
                .getOrDefault( action.action, new Action(action.action) );

            existingAction.addTime( action.time );

            actionsByName.get().put( action.action, existingAction );
        }
    }

    //TODO: return a single string here?
    public List<String> getStats() {
        synchronized ( this ) {
            return actionsByName.get()
                .entrySet()
                .stream()
                .map( entry -> String.format( "{ \"action\": \"%s\", \"avg\": %d }", entry.getValue().name, entry.getValue().getAvg() ) )
                .collect( Collectors.toList() );
        }
    }

    private ActionJson deserialize(String json) {
        return new Gson().fromJson( json, ActionJson.class );
    }

    //todo: change the naming of this
    static class ActionJson {
        private String action;
        private int time;
    }

    static class Action {
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
