package at.xidev.bikechallenge.persistence;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**This class handles the http connection to the server.
 *
 * @author Rick Spiegl/XiDev
 *
 */
public class RESTClient {

    private static final String SERVER_IP = "http://138.232.65.231:8080/BikeChallengeWeb/";

    private static final String GET_TAG = "REST Get";
    private static final String POST_TAG = "REST Post";

    private static HttpClient client;
    private static HttpContext context;

    public static String get(String relPath) throws IOException {
        client = new DefaultHttpClient();
        context = new BasicHttpContext();
        HttpGet get = new HttpGet(SERVER_IP + relPath);

        String response = "";
        HttpResponse resp = client.execute(get, context);
        response = convertStreamToString(resp.getEntity().getContent());
        Log.d(GET_TAG, "Response is: " + response);

        Log.d(GET_TAG, "Status Line: " + resp.getStatusLine().getStatusCode());
        return response;
    }

    public static String post(String json, String relPath) throws IOException {
        client = new DefaultHttpClient();
        context = new BasicHttpContext();
        HttpPost post = new HttpPost(SERVER_IP + relPath);

        Log.d(POST_TAG, json);

        post.setEntity(new StringEntity(json));
        post.setHeader("Content-type", "application/json");

        String response = "";
        HttpResponse resp = client.execute(post, context);
        InputStream is = resp.getEntity().getContent();
        response = convertStreamToString(is);
        Log.d(POST_TAG, "Response is: "+ response);

        Log.d(POST_TAG, "Status line: "+resp.getStatusLine().getStatusCode());

        return response;
    }

    private static String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
}
