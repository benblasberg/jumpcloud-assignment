package org.example.assignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionTest {

    Action action;

    @BeforeEach
    public void beforeEach() {
        action = new Action( "actionName" );
    }

    @Test
    public void testGetAvg_noTimesAdded() {
        assertEquals( 0, action.getAvg() );
    }

    @Test
    public void getName() {
        assertEquals( "actionName", action.getName() );
    }

    @Test
    public void testAddTime_andGetAvg() {
        action.addTime( 5 );
        assertEquals( 5, action.getAvg() );
        action.addTime( 11 );
        assertEquals( 8, action.getAvg() );
    }
}