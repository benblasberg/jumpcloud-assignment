## Assumptions

1. Integer precision is enough for time averages
2. Order is not important on stats return
3. Action names can be any string
4. Times must be greater than zero
5. Times are within the range of a java integer, and their sum will not surpass the range of a java integer.
6. The number of action entries for an action will not surpass the range of a java integer.
7. The getStats() response json only needs to be valid json, and not formatted in any way.


## Running the project

### Prerequisites

This project requires maven 3.6.0+ to build and run the tests.

It also requires java 1.8

To build this project and run the tests, you can run:

`mvn clean install`

To view javadoc for this project, run `mvn javadoc:javadoc` and then open `target/site/apidocs/index.html`


## About

To use this library, consumers should create their own instance of the `Action` class.

The Action class contains two public methods that are available to consumers:

```java
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
public void addAction(final String json);

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
public String getStats();
```

