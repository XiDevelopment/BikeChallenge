package at.xidev.bikechallenge.test;

import junit.framework.TestCase;

import at.xidev.bikechallenge.model.User;
import at.xidev.bikechallenge.persistence.DataFacade;

/**
 * Created by int3r on 04.05.2014.
 */
public class DataFacadeTests extends TestCase {
    private DataFacade facade;

    public void setUp() throws Exception {
        facade = DataFacade.getInstance();
    }

    public void testSetUser() throws Exception {
    }

    public void tearDown() throws Exception {
        facade = null;
    }
}