package at.xidev.bikechallenge.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import at.xidev.bikechallenge.model.Route;
import at.xidev.bikechallenge.model.Statistic;
import at.xidev.bikechallenge.model.User;
import at.xidev.bikechallenge.persistence.DataFacade;
import at.xidev.bikechallenge.view.LoginActivity;

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
        this.user = user;
        return user != null;
    }

    public boolean register(User user) throws IOException {
        String resp = DataFacade.getInstance().registerUser(user);
        return resp.equals("OK");
    }

    public void logout(Context context) {
        SharedPreferences settings = context.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("loggedIn", false);
        editor.commit();
    }

    public boolean isLoggedIn(Context context) {
        SharedPreferences settings = context.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        return settings.getBoolean("loggedIn", false);
    }

    public List<String> getLoggedInCredentials(Context context) {
        SharedPreferences settings = context.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        List<String> list = new ArrayList<String>();
        list.add(settings.getString("username", ""));
        list.add(settings.getString("password", ""));
        return list;
    }

    public User getUser() {
        return user;
    }

    public Drawable getAvatar(User user) {
        return null;
    }

    public boolean setAvatar(Drawable avatar) {
        return false;
    }

    public User getFriend(String username) {
        for (User u : friends)
            if (u.getName().equals(username))
                return u;

        return null;
    }

    public List<User> getFriends() {
        return friends;
    }

    public List<User> getFriends(SortBy sortBy) {
        //List<User> user = DataFacade.getInstance().getFriends();
        return friends;
    }

    // TODO
    public List<User> getFriendRequests() {
        List<User> testList = new ArrayList<>();
        testList.add(new User("test1", "bla", "bla"));
        testList.add(new User("test2", "bla", "bla"));
        return testList;
    }

    // TODO
    public boolean requestFriend(String username) {
        //String resp = DataFacade.getInstance().addFriend(username);
        //return resp.equals("OK");
        return false;
    }

    // TODO javadoc
    public boolean acceptFriend(User user) {
        try {
            String resp = DataFacade.getInstance().answerRequest(user.getName(), true);
            return false; // TODO response
        } catch (IOException e) {
            return false;
        }
    }

    // TODO javadoc
    public boolean declineFriend(User user) {
        try {
            String resp = DataFacade.getInstance().answerRequest(user.getName(), false);
            return false; // TODO response
        } catch (IOException e) {
            return false;
        }
    }

    public boolean removeFriend(User user) {
        // TODO server

        friends.remove(user);
        return true;
    }

    public List<Route> getRoutes(User user) throws IOException {
        return DataFacade.getInstance().getRoutes();
    }

    public boolean saveRoute(Route route) throws IOException {
        String resp = DataFacade.getInstance().saveRoute(route);
        return resp.equals("OK");
    }

    /**
     * Statistic of current user
     *
     * @return the statistic
     */
    public Statistic getStatistic() {
        return getStatistic(this.user);
    }

    /**
     * The statistic of the specified user
     *
     * @param user the user
     * @return the statistic
     */
    public Statistic getStatistic(User user) {
        // TODO test statistic obj
        Statistic s = new Statistic();
        s.setAvgDistance(20);
        s.setAvgTime(200);
        s.setEmission(20012);
        s.setFuel(1232);
        s.setLongestDistance(12322);
        s.setTotalDistance(123212312);
        s.setTotalTime(122322);

        List<Float> l = new ArrayList<>();
        l.add(22.3f);
        l.add(2f);
        l.add(232.7f);
        l.add(120.3f);
        l.add(122.65f);
        l.add(2.8f);
        l.add(212f);
        s.setLast7Days(l);

        return s;
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
