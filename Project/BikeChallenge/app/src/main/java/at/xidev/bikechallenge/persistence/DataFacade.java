package at.xidev.bikechallenge.persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import at.xidev.bikechallenge.model.Statistic;
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
    private String password = "";
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
        String resp;
        // + "/" + password
        resp = RESTClient.get("user/"+username);
        if(!resp.equals("Error")) {
            this.setUsername(username);
            this.password = password;
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
        String resp;
        resp = RESTClient.get("route/" + username);
        if(!resp.equals("Error")) {
            return gson.fromJson(resp, new TypeToken<List<Route>>(){}.getType());
        }
        return null;
    }

    /**
     * Gets a route with the specified routeID.
     * @param routeId ID of the route
     * @return a route object
     */
    public Route getRoute(Integer routeId) throws IOException {
        String resp;
        resp = RESTClient.get("route/"+username+"/"+routeId);
        if(!resp.equals("Error"))
            return gson.fromJson(resp, Route.class);
        return null;
    }

    /**
     * Saves the route on the server
     * @param route route object to save
     * @return the response of the request
     */
    public String saveRoute(Route route) throws IOException {
        String resp = "Error";
        resp = RESTClient.post(gson.toJson(route, Route.class), "route/"+username);
        return resp;
    }

    /**
     * Deletes a route on the server.
     * @param routeId ID of the route
     * @return the response of the request
     */
    public String deleteRoute(Integer routeId) throws IOException {
        String resp = "Error";
        resp = RESTClient.delete("route/"+username+"/"+routeId);

        return resp;
    }

    /**
     * Gets a list of all friends from the user.
     * @return a list of friend objects
     */
    public List<User> getFriends() throws IOException {
        String resp;
        resp = RESTClient.get("friend/" + username);
        if(!resp.equals("Error"))
            return gson.fromJson(resp, new TypeToken<List<User>>(){}.getType());
        return null;
    }

    /**
     * Sends a friendrequest to add a friend to the server.
     * @param friend username of the friend to add
     * @return the response of the request
     */
    public String addFriend(String friend) throws IOException {
        String resp = "Error";
        resp = RESTClient.post("", "friend/"+username+"/request/"+friend);
        return resp;
    }

    /**
     * Answers a friend request.
     * @param friend username of the friend to add
     * @param accept true if accepted, false if denies
     * @return the response of the request
     */
    public String answerRequest(String friend, boolean accept) throws IOException {
        String resp = "Error";
        String answer;
        if(accept)
            answer = "accept";
        answer = "refuse";

        resp = RESTClient.post("","friend/"+username+"/request/"+friend+"/"+answer);
        return resp;
    }

    /**
     * Gets the statistics from the server from the currently user logged in.
     * @return a object in form of Statistic
     * @throws IOException
     */
    public Statistic getStatistics() throws IOException {
       return this.getStatistics(this.username);
    }

    /**
     * Gets the statistics from the given user.
     * @param username username of the user to get statistics for
     * @return a object in form of Statistic
     * @throws IOException
     */
    public Statistic getStatistics(String username) throws IOException {
        String resp = "Error";
        resp = RESTClient.get("/statistic/"+username);
        if(!resp.equals("Error"))
            return gson.fromJson(resp, Statistic.class);
        return null;
    }

    public List<User> getRequests() throws IOException {
        String resp = "Error";
        resp = RESTClient.get("/friend/"+this.username+"/pending");
        if(!resp.equals("Error"))
            return gson.fromJson(resp,new TypeToken<List<User>>(){}.getType());
        return null;
    }
}
