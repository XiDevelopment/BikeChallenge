package at.xidev.bikechallenge.core;

import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import at.xidev.bikechallenge.model.Route;
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
    private List<User> friends;

    private AppFacade() {
        this.friends = new ArrayList<User>();
        // TODO get from database
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            friends.add(new User("Test" + i, null, rnd.nextInt(999999), null));
        }

        // Sort by Points
        sortFriendList(SortBy.Score);
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

    public boolean register(String name, String password, String email){
        return false;
    }

    public void logout(){}

    public boolean isLoggedIn(){
        return false;
    }

    public User getUser() {
        return user;
    }
    public Drawable getAvatar(User user) {
        return null;
    }
    public boolean setAvatar(Drawable avatar){
        return false;
    }

    public User getFriend(int id) {
        for(User u : friends)
            if(u.getId() == id)
                return u;

        return null;
    }
    public List<User> getFriends(){
        return friends;
    }
    public List<User> getFriends(SortBy sortBy){
        return friends;
    }
    public List<User> getFriendRequests(){
        return null;
    }
    public  boolean requestFriend(String username){
        return false;
    }
    public boolean acceptFriend(User user){
        return false;
    }
    public  boolean removeFriend(User user){
        return false;
    }

    public List<Route> getRoutes(User user){
        return null;
    }
    public boolean saveRoute(Route route){
        return false;
    }

   /*Statistic getStatistic(User user){
        return null;
    }*/


    @Deprecated
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Registers an user on the server. Returns true if it was successful and false if not.
     *
     * @param user user to register
     * @return true if successful, false if not
     */
    @Deprecated
    public boolean registerUser(User user) throws IOException {
        String resp = DataFacade.getInstance().registerUser(user);
        return !resp.equals("Error");
    }

    private void sortFriendList(SortBy sortBy) {
        switch (sortBy) {
            case Name:
                Collections.sort(friends, new Comparator<User>() {
                    public int compare(User o1, User o2) {
                        return o2.getName().compareTo(o1.getName());
                    }
                });
                break;
            case Score:
                Collections.sort(friends, new Comparator<User>() {
                    public int compare(User o1, User o2) {
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
}
