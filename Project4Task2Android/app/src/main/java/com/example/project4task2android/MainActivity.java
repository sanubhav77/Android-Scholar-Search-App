/**
 * MainActivity.java
 * Entry-point Activity for ScholarSearch. Takes a user search query,
 * calls the cloud-deployed web service, parses the JSON reply, and
 * displays matching arXiv papers in a RecyclerView. Repeatable.
 * Author: Anubhav Sharma
 * AndrewID: anubhav3
 */
package com.example.project4task2android;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    // IMPORTANT: Update this URL if  Codespace restarts with a new URL.

    private static final String SERVER_BASE_URL =
            "https://stunning-fishstick-xvvvpqj46w5hvx7g-8080.app.github.dev";
    private static final String SEARCH_PATH = "/search";

    private EditText queryInput;
    private Button searchButton;
    private TextView statusText;
    private RecyclerView resultsList;
    private PaperAdapter adapter;
    private final List<Paper> papers = new ArrayList<>();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queryInput = findViewById(R.id.queryInput);
        searchButton = findViewById(R.id.searchButton);
        statusText = findViewById(R.id.statusText);
        resultsList = findViewById(R.id.resultsList);

        adapter = new PaperAdapter(papers);
        resultsList.setLayoutManager(new LinearLayoutManager(this));
        resultsList.setAdapter(adapter);

        searchButton.setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
        String query = queryInput.getText().toString().trim();

        // Requirement 3: validate mobile app input
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a search topic", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Searching...");
        papers.clear();
        adapter.notifyDataSetChanged();

        // Network call on a background thread
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String encoded = URLEncoder.encode(query, "UTF-8");
                URL url = new URL(SERVER_BASE_URL + SEARCH_PATH
                        + "?query=" + encoded + "&maxResults=10");
                android.util.Log.i("ScholarSearch", "Requesting: " + url.toString());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                // Helps the dashboard log useful device info
                conn.setRequestProperty("User-Agent", "ScholarSearch/1.0 (" + Build.MODEL + ")");

                int code = conn.getResponseCode();
                if (code != 200) {
                    showStatus("Server error (HTTP " + code + "). Please try again.");
                    return;
                }

                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                }

                parseAndDisplay(body.toString());

            } catch (Exception e) {
                android.util.Log.e("ScholarSearch", "Request failed", e);
                final String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
                showStatus(msg);
            }
             finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void parseAndDisplay(String json) throws org.json.JSONException {
        JSONObject root = new JSONObject(json);
        int count = root.optInt("count", 0);
        JSONArray arr = root.optJSONArray("papers");
        List<Paper> fresh = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                fresh.add(new Paper(
                        o.optString("title", ""),
                        o.optString("authors", ""),
                        o.optString("published", ""),
                        o.optString("categories", ""),
                        o.optString("summary", ""),
                        o.optString("pdfUrl", ""),
                        o.optString("arxivId", "")
                ));
            }
        }
        uiHandler.post(() -> {
            papers.clear();
            papers.addAll(fresh);
            adapter.notifyDataSetChanged();
            statusText.setText(count == 0 ? "No results found." : "Found " + count + " papers.");
        });
    }

    private void showStatus(String msg) {
        uiHandler.post(() -> statusText.setText(msg));
    }
}