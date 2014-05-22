package at.xidev.bikechallenge.persistence;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
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

    private static final String TAG = "REST";

    private static HttpClient client;
    private static HttpContext context;

    /**
     * Executes a HTTP-GET request to the server and the relative path given.
     * @param relPath which specific path to connect to
     * @return the answer from the request, a String of a JSON object
     * @throws IOException if the reader is closed or another I/O exception occurs
     */
    public static String get(String relPath) throws IOException {
        client = new DefaultHttpClient();
        context = new BasicHttpContext();
        HttpGet get = new HttpGet(SERVER_IP + relPath);

        Log.d(TAG, "GET:");

        String resp = "";
        HttpResponse response = client.execute(get, context);
        resp = convertStreamToString(response.getEntity().getContent());
        Log.d(TAG, "Response is: " + resp);

        Log.d(TAG, "Status Line: " + response.getStatusLine().getStatusCode());
        return resp;
    }

    /**
     * Execute a HTTP-POST request to the server and the relative path given.
     * @param json an json string of the object
     * @param relPath which specific path to connect to
     * @return the answer from the request, either OK or Error
     * @throws IOException if the reader is closed or another I/O exception occurs
     */
    public static String post(String json, String relPath) throws IOException {
        client = new DefaultHttpClient();
        context = new BasicHttpContext();
        HttpPost post = new HttpPost(SERVER_IP + relPath);

        Log.d(TAG, "POST:");
        Log.d(TAG, json);

        post.setEntity(new StringEntity(json));
        post.setHeader("Content-type", "application/json");

        String resp = "";
        HttpResponse response = client.execute(post, context);
        InputStream is = response.getEntity().getContent();
        resp = convertStreamToString(is);
        Log.d(TAG, "Response is: "+ resp);

        Log.d(TAG, "Status line: "+response.getStatusLine().getStatusCode());

        return resp;
    }

    /**
     * Executes a HTTP-DELETE request to the server and the relative path given.
     * @param relPath which specific path to connect to
     * @return the answer from the request, either OK or Error
     * @throws IOException if the reader is closed or another I/O exception occurs
     */
    public static String delete(String relPath) throws IOException {
        String resp = "";
        client = new DefaultHttpClient();
        context = new BasicHttpContext();

        Log.d(TAG, "DELETE");

        HttpDelete delete = new HttpDelete(SERVER_IP + relPath);
        HttpResponse response = client.execute(delete, context);
        InputStream is = response.getEntity().getContent();
        resp = convertStreamToString(is);

        Log.d(TAG, "Response is: " + resp);
        Log.d(TAG, "Status line: " + response.getStatusLine().getStatusCode());

        return resp;
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
