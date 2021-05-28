package org.example.assignment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ActionsTest {
    private static final String NULL = null;

    Actions actions;
    Gson gson = new Gson();

    private ExecutorService createExecutorService(int numberOfThreads) {
        return Executors.newFixedThreadPool(numberOfThreads);
    }

    private String createAddActionJson(String action, int time) {
        return String.format( "{ \"action\": \"%s\", \"time\": %d }", action, time );
    }

    private String createActionAverageJson(String action, int average) {
        return String.format( "{\"action\":\"%s\",\"avg\":%d}", action, average );
    }

    private List<String> generateActions(int n) {
        List<String> actions = new ArrayList<>(n);
        for (int i=0; i < n; i++) {
            actions.add(
                createAddActionJson( i % 2 == 0? "jump" : "run", ThreadLocalRandom.current().nextInt(499) + 1 ));
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
        actions.addAction( createAddActionJson( "jump", 100 ) );
        actions.addAction( createAddActionJson( "run", 75 ) );
        actions.addAction( createAddActionJson( "jump", 200 ) );

        String actualAvgsJson = actions.getStats();

        final String expectedAvgsJson =
            String.format( "[%s,%s]",
                createActionAverageJson( "run", 75 ),
                createActionAverageJson( "jump", 150 ) );

        assertEquals(expectedAvgsJson, actualAvgsJson);
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 4, 10, 50, 100 })
    public void testThreadedNThreads(int threads) {
        List<String> actionJsons = generateActions( 1000 );

        //calculate expected results first, using no threads logic
        actionJsons.forEach( a -> actions.addAction( a ) );
        String expectedJson = actions.getStats();

        actions = new Actions();

        ExecutorService executorService = createExecutorService( threads );

        List<Future> futures = actionJsons.stream()
            .map( json -> executorService.submit( () -> {actions.addAction( json ); actions.getStats();} ) )
            .collect( Collectors.toList());

        waitForFuturesToComplete( futures );
        executorService.shutdown();

        assertEquals( expectedJson, actions.getStats() );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "} )
    public void testAdd_invalidJson(String json) {
        IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> actions.addAction( json ) );

        assertEquals( "json cannot be null blank or empty, but was: " + json, exception.getMessage() );
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"time\": 5}", "{\"action\": \"\" }", "{\"action\": \" \"}"} )
    public void testAdd_invalidJsonAction(String json) {
        IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> actions.addAction( json ) );

        assertTrue( exception.getMessage().startsWith( "action cannot be null blank or empty, but was: " ) );
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"action\": \"run\"}", "{\"action\": \"run\", \"time\":0 }", "{\"action\": \"run\", \"time\": -148}"} )
    public void testAdd_invalidJsonTime(String json) {
        IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> actions.addAction( json ) );

        assertTrue( exception.getMessage().startsWith( "time must be positive, but was: " ) );
    }

    @Test
    public void testAdd_nullJson() {
        IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> actions.addAction( null ) );

        assertEquals( "json cannot be null blank or empty, but was: null", exception.getMessage() );
    }

    @Test
    public void testStats_empty() {
        assertEquals( "[]", actions.getStats() );
    }
}