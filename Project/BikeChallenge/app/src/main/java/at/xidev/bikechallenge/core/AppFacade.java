package at.xidev.bikechallenge.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import at.xidev.bikechallenge.model.Friend;

/**
 * Created by int3r on 14.04.2014.
 */
public class AppFacade {
    public enum SortBy {
        Name,
        Points,
        Km,
    }

    private static AppFacade ourInstance = new AppFacade();

    public static AppFacade getInstance() {
        return ourInstance;
    }

    private List<Friend> friends;

    private AppFacade() {
        this.friends = new ArrayList<Friend>();
        // TODO get from database
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            friends.add(new Friend(i, "Test" + i, rnd.nextInt(999999)));
        }

        // Sort by Points
        sortFriendList(SortBy.Points);
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
            case Points:
                Collections.sort(friends, new Comparator<Friend>() {
                    public int compare(Friend o1, Friend o2) {
                        if (o2.getPoints() > o1.getPoints())
                            return +1;
                        if (o2.getPoints() < o1.getPoints())
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

    public Friend addFriend(int id) {
        // TODO Get friend from user
        // for now, just make a new Random friend
        Random rnd = new Random();
        Friend friend = new Friend(id, "Created" + id, rnd.nextInt(999999));

        // Add to FriendsList
        friends.add(friend);

        // Sort by Points
        sortFriendList(SortBy.Points);

        return friend;
    }

    public Friend addFriend(String search) {
        // TODO Search for User in Db und return id...

        Random rnd = new Random();
        int id = rnd.nextInt(200);

        return addFriend(id);
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
