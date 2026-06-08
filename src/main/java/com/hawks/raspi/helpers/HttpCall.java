package com.hawks.raspi.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpCall {

    /**
     * implementation("com.google.code.gson:gson:2.10.1")
     */

    private static final String TAG = "varun ";
    private static final int CONNECT_TIMEOUT_MS = 10_000;  // 10 seconds
    private static final int READ_TIMEOUT_MS = 15_000;     // 15 seconds
    private static final int BUFFER_SIZE = 8_192;

    public static final Gson gson = new Gson();

    /**
     * Performs an HTTP request and returns the response body as a String,
     * or null if the request fails.
     * <p>
     * Must be called from a background thread (AsyncTask, coroutine, etc.)
     *
     * @param url     The endpoint URL.
     * @param method  HTTP method (default: "GET").
     * @param headers Optional map of extra request headers.
     * @return Response body string, or null on failure.
     */
    public String get(String url, String method, Map<String, String> headers) {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            connection.connect();

            int responseCode = connection.getResponseCode();

            System.out.println(TAG + "Response code for " + url + ": " + responseCode);

            if (responseCode < 200 || responseCode > 299) {
                System.out.println(TAG + "Non-success HTTP " + responseCode + " for " + url + " message: " + connection);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
            }

            String result = response.toString();
            System.out.println(TAG + "get: response: " + result);
            if (responseCode < 200 || responseCode > 299)
                return null;
            else return result;

        } catch (MalformedURLException e) {
            System.out.println(TAG + "Malformed URL: " + url);
            return null;
        } catch (SocketTimeoutException e) {
            System.out.println(TAG + "Request timed out: " + url);
            return null;
        } catch (IOException e) {
            System.out.println(TAG + "I/O error for " + url);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Overload: method only (no headers)
    public String get(String url, String method) {
        return get(url, method, Collections.emptyMap());
    }

    // Overload: defaults (GET, no headers)
    public String get(String url) {
        return get(url, "GET", Collections.emptyMap());
    }

    /**
     * Performs an HTTP GET and deserializes the JSON response into the given type.
     * <p>
     * Usage examples:
     * JokeModel joke = http.getAs(url, JokeModel.class);
     * List<JokeModel> list = http.getAs(url, new TypeToken<List<JokeModel>>(){}.getType());
     * HashMap<String, Object> map = http.getAs(url, new TypeToken<HashMap<String, Object>>(){}.getType());
     */
    public <T> T getAs(String url, Class<T> clazz) {
        String json = get(url);
        if (json == null) return null;
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            System.out.println(TAG + "JSON parse error for " + url);
            return null;
        }
    }

    public <T> T getAs(String url, Type type) {
        String json = get(url);
        if (json == null) return null;
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            System.out.println(TAG + "JSON parse error for " + url);
            return null;
        }
    }

    public <T> T getAs(String url, Class<T> clazz, String method, Map<String, String> headers) {
        String json = get(url, method, headers);
        if (json == null) return null;
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            System.out.println(TAG + "JSON parse error for " + url);
            return null;
        }
    }

    // New overload accepting Type (add this)
    public <T> T getAs(String url, Type type, String method, Map<String, String> headers) {
        try {
            String jsonResponse = get(url, method, headers);
            return gson.fromJson(jsonResponse, type);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, String> getDummyHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Referer", "https://google.com");
        headers.put("Connection", "keep-alive");
        return headers;
    }

    public Map<String, String> getDummyHeaderForWindows() {
        Map<String, String> headers = new HashMap<>();

        // Windows 11 + Chrome 124 User Agent
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");

        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate, br");

        // Security headers Chrome sends on Windows 11
        headers.put("Sec-CH-UA", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"");
        headers.put("Sec-CH-UA-Mobile", "?0");
        headers.put("Sec-CH-UA-Platform", "\"Windows\"");
        headers.put("Sec-Fetch-Dest", "document");
        headers.put("Sec-Fetch-Mode", "navigate");
        headers.put("Sec-Fetch-Site", "none");
        headers.put("Sec-Fetch-User", "?1");

        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("Connection", "keep-alive");
        headers.put("Referer", "https://www.google.com/");
        headers.put("Cache-Control", "max-age=0");

        return headers;
    }
}
