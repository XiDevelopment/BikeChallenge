package at.xidev.bikechallenge.persistence;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import at.xidev.bikechallenge.model.Friend;
import at.xidev.bikechallenge.model.User;
import at.xidev.bikechallenge.model.Route;

/**This class provides functionalities to communicate with the server.
 * For that it uses the class RESTClient. With the first use of login
 * the username is saved for further calls.
 *
 * @author Rick Spiegl/XiDev
 *
 * @see at.xidev.bikechallenge.persistence.RESTClient
 */
public class DataFacade {

    private static DataFacade instance = new DataFacade();
    private final String TAG = "DataFacade";
    private String username = "";
    private Gson gson = new Gson();

    public static DataFacade getInstance() {return instance;}

    /**
     * Sets the username of this DataFacade.
     * @param username username for further requests
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user data from the server with the specified username and password.
     * @param username username of the user
     * @param password password of the user
     * @return an user object if the get request was successfull or null if not
     */
    public User getUser(String username, String password) throws IOException {
        String resp = "Error";
        // + "/" + password
        resp = RESTClient.get("user/"+username);
        if(!resp.equals("Error")) {
            this.setUsername(username);
            return gson.fromJson(resp, User.class);
        }
        else
            return null;
    }

    /**
     * Registers the user object on the server. Returns the response from the query.
     * @param user the user object to register
     * @return the response from the request
     */
    public String registerUser(User user) throws IOException {
        String resp = "Error";
        resp = RESTClient.post(gson.toJson(user, User.class), "user/" + user.getName());

        return resp;
    }

    /**
     * Gets all routes of the user.
     * @return a list of the routes
     */
    public List<Route> getRoutes() throws IOException {
        return null;
    }

    /**
     * Gets a route with the specified routeID.
     * @param routeId ID of the route
     * @return a route object
     */
    public Route getRoute(Integer routeId) throws IOException {
        return null;
    }

    /**
     * Saves the route on the server
     * @param route route object to save
     * @return the response of the request
     */
    public String saveRoute(Route route) throws IOException {
        return null;
    }

    /**
     * Deletes a route on the server.
     * TODO: not yet implemented on the server
     * @param routeId ID of the route
     * @return the response of the request
     */
    public String deleteRoute(Integer routeId) throws IOException {
        return null;
    }

    /**
     * Gets a list of all friends from the user.
     * @return a list of friend objects
     */
    public List<Friend> getFriends() throws IOException {
        return null;
    }

    /**
     * Sends a friendrequest to add a friend to the server.
     * @param friend username of the friend to add
     * @return the response of the request
     */
    public String addFriend(String friend) throws IOException {
        return null;
    }

    /**
     * Answers a friend request.
     * @param friend username of the friend to add
     * @param accept true if accepted, false if denies
     * @return the response of the request
     */
    public String answerRequest(String friend, boolean accept) throws IOException {
        return null;
    }

    /**
     * Gets the statistics from the server.
     * //TODO: decide if it should be implemented
     * @return
     */
    public String getStatistics() throws IOException {
        return null;
    }
}
