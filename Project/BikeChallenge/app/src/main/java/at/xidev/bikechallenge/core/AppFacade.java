package at.xidev.bikechallenge.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.xidev.bikechallenge.model.Route;
import at.xidev.bikechallenge.model.Statistic;
import at.xidev.bikechallenge.model.User;
import at.xidev.bikechallenge.persistence.DataFacade;
import at.xidev.bikechallenge.view.LoginActivity;
import at.xidev.bikechallenge.view.R;

/**
 * Created by int3r on 14.04.2014.
 */
public class AppFacade {
    private static AppFacade ourInstance = new AppFacade();
    public static final int NUM_AVATARS = 40;
    private User user;

    public static AppFacade getInstance() {
        return ourInstance;
    }

    /**
     * Logs the user at the server in with the credentials and returns his user object.
     *
     * @param username username to log in
     * @param password password to log in
     * @return true if successful, false otherwise
     * @throws IOException
     */
    public boolean login(String username, String password) throws IOException {
        User user = DataFacade.getInstance().getUser(username, password);
        this.user = user;
        return user != null;
    }

    /**
     * Updates the current user object
     */
    public boolean updateUser() throws IOException {
        user = DataFacade.getInstance().updateUser();
        return user != null;
    }

    /**
     * Registers an user at the server.
     *
     * @param user user object to register at the server
     * @return true if successful, false otherwise
     * @throws IOException
     */
    public boolean register(User user) throws IOException {
        String resp = DataFacade.getInstance().registerUser(user);
        return resp.equals("OK");
    }

    /**
     * Logs the current user out with removing the loggedIn state from the Preferences.
     *
     * @param context context of the app
     */
    public void logout(Context context) {
        SharedPreferences settings = context.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("loggedIn", false);
        editor.commit();
    }

    /**
     * Checks if the user is already logged into the app.
     *
     * @param context context of the app
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn(Context context) {
        SharedPreferences settings = context.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        return settings.getBoolean("loggedIn", false);
    }

    /**
     * Gets the credentials out of the preferences.
     *
     * @param context context of the app
     * @return a list of Strings with the order: username, password
     */
    public List<String> getLoggedInCredentials(Context context) {
        SharedPreferences settings = context.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        List<String> list = new ArrayList<String>();
        list.add(settings.getString("username", ""));
        list.add(settings.getString("password", ""));
        return list;
    }

    public void putLoggedInCredentials(Context context, String username, String password) {
        SharedPreferences settings = context.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("loggedIn", true);
        editor.commit();
    }

    /**
     * Returns the current logged in user.
     *
     * @return user object of current user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sends the new profile picture id to the server.
     *
     * @param avatarId the id of the picture
     * @return true if successful, false otherwise
     */
    public boolean setAvatar(Integer avatarId) throws IOException {
        String resp = DataFacade.getInstance().setAvatar(avatarId);
        return resp.equals("OK");
    }

    /**
     * Returns an user object given the specified username.
     *
     * @param username username of the user
     * @return user object
     * @throws IOException
     */
    public User getFriend(String username) throws IOException {
        List<User> list = DataFacade.getInstance().getFriends();
        for (User u : list)
            if (u.getName().equals(username))
                return u;

        return null;
    }

    /**
     * Returns a list of user (friends) of the current user.
     *
     * @return a list of user objects
     * @throws IOException
     */
    public List<User> getFriends() throws IOException {
        return getFriends(SortBy.Score);
    }

    public List<User> getFriends(SortBy sortBy) throws IOException {
        List<User> friends = DataFacade.getInstance().getFriends();
        friends = sortFriendList(friends, sortBy);
        return friends;
    }


    /**
     * Returns all pending friend request from the current user.
     *
     * @return a list of user objects
     * @throws IOException
     */
    public List<User> getFriendRequests() throws IOException {
        List<User> list = DataFacade.getInstance().getRequests();
        return list;
    }

    /**
     * Sends a request to an other user specified by the username.
     *
     * @param username username of the user
     * @return true if successful, false otherwise
     * @throws IOException
     */
    public boolean requestFriend(String username) throws IOException {
        String resp = DataFacade.getInstance().addFriend(username);
        return resp.equals("OK");
    }

    /**
     * Accepts a friend request from the given user.
     *
     * @param user user to accept the friend request
     * @return true if successful, false otherwise
     * @throws IOException
     */
    public boolean acceptFriend(User user) throws IOException {
        String resp = DataFacade.getInstance().answerRequest(user.getName(), true);
        return resp.equals("OK");
    }

    /**
     * Declines a friend request from the given user.
     *
     * @param user user to refuse the friend request
     * @return true if successful, false otherwise
     * @throws IOException
     */
    public boolean declineFriend(User user) throws IOException {
        String resp = DataFacade.getInstance().answerRequest(user.getName(), false);
        return resp.equals("OK");
    }

    /**
     * Removes a friend from the friendlist.
     *
     * @param user user to delete from the friendlist
     * @return true if successful, false otherwise
     * @throws IOException
     */
    public boolean removeFriend(User user) throws IOException {
        return this.declineFriend(user);
    }

    /**
     * Returns all Routes from the specified user.
     *
     * @param user user to get routes from
     * @return a list of route objects
     * @throws IOException
     */
    public List<Route> getRoutes(User user) throws IOException {
        return DataFacade.getInstance().getRoutes();
    }

    /**
     * Uploads a route to the server.
     *
     * @param route route object to save
     * @return true if successful, false otherwise
     * @throws IOException
     */
    public boolean saveRoute(Route route) throws IOException {
        String resp = DataFacade.getInstance().saveRoute(route);
        return resp.equals("OK");
    }

    /**
     * Returns the statistics from the current user.
     *
     * @return a statistic object
     */
    public Statistic getStatistic() throws IOException {
        return DataFacade.getInstance().getStatistics();
    }

    /**
     * Returns the statistics from the specified user.
     *
     * @param user the user to get statistics from
     * @return a statistic object
     */
    public Statistic getStatistic(User user) throws IOException {
        return DataFacade.getInstance().getStatistics(user.getName());
    }

    /**
     * Format milliseconds to elapsed time format.
     *
     * @param milisDiff time difference in milliseconds
     * @return Human readable string representation - eg. 2 days, 14 hours, 5 minutes
     */
    public String formatTimeElapsedSinceMillisecond(long milisDiff) {
        if (milisDiff < 1000) {
            return "0 seconds";
        }

        String formattedTime = "";
        long secondInMillis = 1000;
        long minuteInMillis = secondInMillis * 60;
        long hourInMillis = minuteInMillis * 60;
        long dayInMillis = hourInMillis * 24;
        long weekInMillis = dayInMillis * 7;
        long monthInMillis = dayInMillis * 30;

        int timeElapsed[] = new int[6];
        // Define time units - plural cases are handled inside loop
        String timeElapsedText[] = {"s", "m", "h", "d", "w", "m"};
        timeElapsed[5] = (int) (milisDiff / monthInMillis); // months
        milisDiff = milisDiff % monthInMillis;
        timeElapsed[4] = (int) (milisDiff / weekInMillis); // weeks
        milisDiff = milisDiff % weekInMillis;
        timeElapsed[3] = (int) (milisDiff / dayInMillis); // days
        milisDiff = milisDiff % dayInMillis;
        timeElapsed[2] = (int) (milisDiff / hourInMillis); // hours
        milisDiff = milisDiff % hourInMillis;
        timeElapsed[1] = (int) (milisDiff / minuteInMillis); // minutes
        milisDiff = milisDiff % minuteInMillis;
        timeElapsed[0] = (int) (milisDiff / secondInMillis); // seconds

        // Only adds 3 significant high valued units
        for (int i = (timeElapsed.length - 1), j = 0; i >= 0 && j < 3; i--) {
            // loop from high to low time unit
            if (timeElapsed[i] > 0) {
                formattedTime += ((j > 0) ? " " : "")
                        + timeElapsed[i]
                        + " " + timeElapsedText[i];
                ++j;
            }
        } // end for - build string

        return formattedTime;
    }

    /**
     * Returns the avatar of the specific id
     *
     * @param avatarId the avatar id
     * @param context  the context of the application for resources
     * @return a drawable avatar
     */
    public Drawable getAvatar(Integer avatarId, Context context) {
        // Image ID must be between 1 and NUM_AVATARS
        if (avatarId != null && avatarId >= 1 && avatarId <= NUM_AVATARS) {
            int resID = context.getResources().getIdentifier("avatar" + avatarId, "drawable", context.getPackageName());
            return context.getResources().getDrawable(resID);
        } else {
            // return default image
            return context.getResources().getDrawable(R.drawable.ic_action_person);
        }
    }

    private List<User> sortFriendList(List<User> friends, SortBy sortBy) {
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
        }
        return friends;
    }

    public enum SortBy {
        Name,
        Score,
    }
}
