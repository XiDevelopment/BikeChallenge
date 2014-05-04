package at.xidev.bikechallenge.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import at.xidev.bikechallenge.model.Friend;
import at.xidev.bikechallenge.model.User;
import at.xidev.bikechallenge.persistence.DataFacade;

/**
 * Created by int3r on 14.04.2014.
 */
public class AppFacade {
    public enum SortBy {
        Name,
        Score,
        Km,
    }

    private static AppFacade ourInstance = new AppFacade();

    public static AppFacade getInstance() {
        return ourInstance;
    }

    private User user;
    private List<Friend> friends;

    private AppFacade() {
        this.friends = new ArrayList<Friend>();
        // TODO get from database
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            friends.add(new Friend(i, "Test" + i, rnd.nextInt(999999)));
        }

        // Sort by Points
        sortFriendList(SortBy.Score);
    }

    /**
     * Current User
     *
     * @return logged in user or null
     * @throws IOException
     */
    public User getUser() throws IOException {
        return user;
    }

    /**
     * Login
     *
     * @param username
     * @param password
     * @return true if successful
     * @throws IOException
     */
    public boolean login(String username, String password) throws IOException {
        User user = DataFacade.getInstance().getUser(username, password);
        this.setUser(user);
        return user != null;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Registers an user on the server. Returns true if it was successful and false if not.
     *
     * @param user user to register
     * @return true if successful, false if not
     */
    public boolean registerUser(User user) throws IOException {
        String resp = DataFacade.getInstance().registerUser(user);
        return !resp.equals("Error");
    }

    public void sortFriendList(SortBy sortBy) {
        switch (sortBy) {
            case Name:
                Collections.sort(friends, new Comparator<Friend>() {
                    public int compare(Friend o1, Friend o2) {
                        return o2.getName().compareTo(o1.getName());
                    }
                });
                break;
            case Score:
                Collections.sort(friends, new Comparator<Friend>() {
                    public int compare(Friend o1, Friend o2) {
                        if (o2.getScore() > o1.getScore())
                            return +1;
                        if (o2.getScore() < o1.getScore())
                            return -1;
                        else
                            return 0;
                    }
                });
                break;
            case Km:
                // TODO implement
                break;
        }
    }

    /***
     * Add friend to friends list given a friend obj
     * @param friend the friend to add
     * @return true if successful, false if not
     */
    public boolean addFriend(Friend friend) {
        // TODO add to server

        // TODO if server response successful

        // Add to friend list
        friends.add(friend);

        // Sort friend list
        sortFriendList(SortBy.Score);

        // TODO if everything successful return true
        return true;
    }

    /**
     * Searches for friends by string, and returns possible matches
     * @param search
     * @return a list with possible friends
     */
    public List<Friend> searchFriend(String search) {
        List<Friend> result = new ArrayList<>();

        // TODO Search for User in Db und return id...

        // For now just create one random friend
        Random random = new Random();
        Integer id = random.nextInt(1000);
        Friend friend = new Friend(id, "TestFriend"+id, random.nextInt(100000));
        result.add(friend);

        friends.add(friend); // TODO remove, just testing purpose

        return result;
    }

    public List<Friend> getFriendsLists() {
        // TODO get from database
        return friends;
    }

    public Friend getFriend(int id) {
        for (Friend friend : friends)
            if (friend.getId() == id)
                return friend;

        return null;
    }

    public boolean removeFriend(Friend friend) {
        // TODO call to database
        return friends.remove(friend);
    }
}
