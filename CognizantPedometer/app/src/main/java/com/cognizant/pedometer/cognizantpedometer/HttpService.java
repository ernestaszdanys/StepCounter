package com.cognizant.pedometer.cognizantpedometer;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ERZD01 on 2016-10-26.
 */

public abstract class HttpService extends AsyncTask<Object, Void, Response> implements HttpServiceClient {

    private final String stringUrl = "https://cognizantstepcounter.azurewebsites.net/api/user";
    private final String requestMethod;
    private final JSONObject jsonObject;
    private Response response;

    public HttpService(String requestMethod, JSONObject jsonObject) {
        this.requestMethod = requestMethod;
        this.jsonObject = jsonObject;
        response = new Response();
    }

    public abstract void onResponseReceived(Response response);

    @Override
    protected void onPostExecute(Response response) {
        onResponseReceived(response);
    }

    @Override
    protected Response doInBackground(Object... params) {
        // params comes from the execute() call: params[0] is the url.
        try {
            return downloadUrl();
        } catch (IOException e) {
            return new Response(400, "Unable to retrieve web page. URL may be invalid.");
        }
    }

    private Response downloadUrl() throws IOException {
        InputStream is = null;
        // Only display the first 100 characters of the retrieved web page content.
        int len = 100;

        try {
            URL url = new URL(stringUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod(requestMethod);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            OutputStream output = new BufferedOutputStream(conn.getOutputStream());
            output.write(jsonObject.toString().getBytes());
            output.flush();

            // Response
            int responseCode = conn.getResponseCode();
            response.setResponseCode(responseCode);
            if (responseCode == 200) {
                is = conn.getInputStream();
            }
            else {
                is = conn.getErrorStream();
            }

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            response.setContent(contentAsString);

            return response;

        } finally {
            // Makes sure that the InputStream is closed after the app is finished using it.
            if (is != null) {
                is.close();
            }
        }
    }

    private String readIt(InputStream stream, int len) throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer).trim();
    }
}