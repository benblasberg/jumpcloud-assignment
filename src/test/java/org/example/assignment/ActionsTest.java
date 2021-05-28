package org.example.assignment;

import java.lang.reflect.Type;
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ActionsTest {
    private static final String NULL = null;

    Actions actions;
    Gson gson = new Gson();
    Type listOfActionAvgType = new TypeToken<ArrayList<Actions.ActionAvg>>() {}.getType();

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

        String actualAvgs = actions.getStats();

        List<ActionAvg> expectedAvgs = Arrays.asList(
            new ActionAvg( "run", 75 ),
            new ActionAvg( "jump", 150 ) );
        assertEquals(gson.toJson( expectedAvgs, listOfActionAvgType ), actualAvgs);
    }

    @ParameterizedTest
    @ValueSource(ints = { 4, 10, 50, 100 })
    public void testThreadedNThreads(int threads) {
        List<String> actionJsons = generateActions( 1000 );

        //calculate expected results first, using no threads logic
        actionJsons.forEach( a -> actions.add( a ) );
        String expectedJson = actions.getStats();

        actions = new Actions();

        ExecutorService executorService = createExecutorService( threads );

        List<Future> futures = actionJsons.stream()
            .map( json -> executorService.submit( () -> {actions.add( json ); actions.getStats();} ) )
            .collect( Collectors.toList());

        waitForFuturesToComplete( futures );

        System.out.println(expectedJson);
        System.out.println(actions.getStats());
        assertEquals( expectedJson, actions.getStats() );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "} )
    public void testAdd_invalidJson(String json) {
        IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> actions.add( json ) );

        assertEquals( "json cannot be null blank or empty, but was: " + json, exception.getMessage() );
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"time\": 5}", "{\"action\": \"\" }", "{\"action\": \" \"}"} )
    public void testAdd_invalidJsonAction(String json) {
        IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> actions.add( json ) );

        assertTrue( exception.getMessage().startsWith( "action cannot be null blank or empty, but was: " ) );
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"action\": \"run\"}", "{\"action\": \"run\", \"time\":0 }", "{\"action\": \"run\", \"time\": -148}"} )
    public void testAdd_invalidJsonTime(String json) {
        IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> actions.add( json ) );

        assertTrue( exception.getMessage().startsWith( "time must be positive, but was: " ) );
    }

    @Test
    public void testAdd_nullJson() {
        IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> actions.add( null ) );

        assertEquals( "json cannot be null blank or empty, but was: null", exception.getMessage() );
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