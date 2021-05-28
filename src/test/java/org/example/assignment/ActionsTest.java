package org.example.assignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ActionsTest {

    Actions actions;
    Gson gson = new Gson();

    private ExecutorService createExecutorService(int numberOfThreads) {
        return Executors.newFixedThreadPool(numberOfThreads);
    }

    private String createJson(String action, int time) {
        return String.format( "{ \"action\": \"%s\", \"time\": %d }", action, time );
    }

    private List<String> generateActions(int n) {
        List<String> actions = new ArrayList<>(n);
        for (int i=0; i < n; i++) {
            actions.add(
                createJson( i % 2 == 0? "jump" : "run", ThreadLocalRandom.current().nextInt(499) + 1 ));
        }
        return actions;
    }

    private void waitForFuturesToComplete(List<Future> futures) {
        futures.forEach( f -> {
            try {
                f.get();
            }
            catch ( InterruptedException e ) {
                e.printStackTrace();
            }
            catch ( ExecutionException e ) {
                e.printStackTrace();
            }
        } );
    }

    @BeforeEach
    public void beforeEach() {
        actions = new Actions();
    }

    @Test
    public void testNonThreaded() {
        actions.add( createJson( "jump", 100 ) );
        actions.add( createJson( "run", 75 ) );
        actions.add( createJson( "jump", 200 ) );

        List<ActionAvg> actualAvgs = actions.getStats()
            .stream()
            .map( s -> gson.fromJson( s, ActionAvg.class ) )
            .collect( Collectors.toList());

        List<ActionAvg> expectedAvgs = Arrays.asList(
            new ActionAvg( "run", 75 ),
            new ActionAvg( "jump", 150 ) );
        assertEquals(expectedAvgs, actualAvgs);
    }

    @ParameterizedTest
    @ValueSource(ints = { 10, 50, 100 })
    public void testThreadedNThreads(int threads) {
        List<String> actionJsons = generateActions( 1000 );

        //calculate expected results first, using no threads logic
        actionJsons.forEach( a -> actions.add( a ) );
        List<String> expectedJson = actions.getStats();

        actions = new Actions();

        ExecutorService executorService = createExecutorService( threads );

        List<Future> futures = actionJsons.stream()
            .map( json -> executorService.submit( () -> actions.add( json ) ) )
            .collect( Collectors.toList());

        waitForFuturesToComplete( futures );

        System.out.println(expectedJson);
        System.out.println(actions.getStats());
        assertEquals( expectedJson, actions.getStats() );
    }

    class ActionAvg {
        String action;
        int avg;

        public ActionAvg(String action, int avg) {
            this.action = action;
            this.avg = avg;
        }

        @Override
        public boolean equals(Object o) {
            if ( this == o ) {
                return true;
            }
            if ( o == null || getClass() != o.getClass() ) {
                return false;
            }
            ActionAvg actionAvg = (ActionAvg) o;
            return avg == actionAvg.avg && Objects.equals( action, actionAvg.action );
        }

        @Override
        public int hashCode() {
            return Objects.hash( action, avg );
        }

        @Override
        public String toString() {
            return "ActionAvg{" +
                "action='" + action + '\'' +
                ", avg=" + avg +
                '}';
        }
    }
}