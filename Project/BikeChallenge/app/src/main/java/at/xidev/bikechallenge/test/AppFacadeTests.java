package at.xidev.bikechallenge.test;

import junit.framework.TestCase;

import java.util.List;

import at.xidev.bikechallenge.core.AppFacade;
import at.xidev.bikechallenge.model.Friend;

/**
 * Created by int3r on 04.05.2014.
 */
public class AppFacadeTests extends TestCase {
    private AppFacade facade;

    public void setUp() throws Exception {
        facade = AppFacade.getInstance();
    }

    public void testFriendAdd() throws Exception {
        final Friend expected = new Friend(77, "Test", 1000);

        facade.addFriend(expected);

        Friend received = facade.getFriend(expected.getId());

        assertEquals(received, expected);
    }

    public void testGetFriendsList() throws Exception {
        assertNotNull(facade.getFriendsLists());
    }

    public void testSortByName() throws Exception {
        facade.sortFriendList(AppFacade.SortBy.Name);

        List<Friend> list = facade.getFriendsLists();
        for (int i = 0; i < list.size() - 1; i++) {
            String a = list.get(i).getName();
            String b = list.get(i + 1).getName();

            assertTrue(a.compareTo(b) > 0);
        }
    }

    public void testSortByScore() throws Exception {
        facade.sortFriendList(AppFacade.SortBy.Score);

        List<Friend> list = facade.getFriendsLists();
        for (int i = 0; i < list.size() - 1; i++) {
            Integer a = list.get(i).getScore();
            Integer b = list.get(i + 1).getScore();

            assertTrue(a.compareTo(b) > 0);
        }
    }

    public void testRegisterUser() throws Exception {
        // try to register self
        facade.getUser();
    }

    // TODO km not implemented
    public void testSortByKm() throws Exception {
    }

    public void tearDown() throws Exception {
        facade = null;
    }
}
