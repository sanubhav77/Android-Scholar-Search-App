/**
 *
 * DashboardServlet.java
 *
 * Serves as the controller for the operations dashboard. Connects to MongoDB Atlas,
 * retrieves the complete history of search logs, and computes five distinct analytics
 * (total requests, average latency, top query, success rate, and total fetched papers).
 * Passes this processed data to dashboard.jsp for final rendering.
 *
 * Thank you to arXiv for use of its open access interoperability.
 *
 * Author: Anubhav Sharma
 * AndrewID: anubhav3
 */
package ds.project4task2;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    // TODO: Paste your MongoDB Atlas connection string here
private static final String CONNECTION_STRING = "mongodb+srv://anubhav3_db_user:REPLACE_WITH_YOUR_KEY@cluster0.24nur6o.mongodb.net/?appName=Cluster0";           
    private static final String DB_NAME = "ScholarSearchDB";
    private static final String COLLECTION_NAME = "SearchLogs";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Document> logs = new ArrayList<>();

        // Analytic Counters
        int totalRequests = 0;
        int successfulLatencyCount = 0;
        long totalLatency = 0;
        int successfulRequests = 0;
        int totalPapersFetched = 0;
        Map<String, Integer> queryCounts = new HashMap<>();

        // Fetch logs and perform aggregation
        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DB_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Retrieve all documents, sorted by timestamp descending (newest first)
            FindIterable<Document> iterable = collection.find().sort(new Document("timestamp", -1));

            for (Document doc : iterable) {
                logs.add(doc);
                totalRequests++;

                // Track successful requests for success rate percentage
                if (doc.getBoolean("success", false)) {
                    successfulRequests++;
                }

                // Track total number of papers ever returned to users
                Integer resCount = doc.getInteger("resultCount");
                if (resCount != null) {
                    totalPapersFetched += resCount;
                }

                // Aggregate valid latencies to calculate an accurate average
                Long latency = doc.getLong("arxivLatencyMs");
                if (latency != null && latency > 0) {
                    totalLatency += latency;
                    successfulLatencyCount++;
                }

                // Aggregate queries to find the most frequent search term
                String query = doc.getString("query");
                if (query != null && !query.trim().isEmpty() && !query.equals("null")) {
                    String normalizedQuery = query.toLowerCase();
                    queryCounts.put(normalizedQuery, queryCounts.getOrDefault(normalizedQuery, 0) + 1);
                }
            }
        } catch (Exception e) {
            request.setAttribute("error", "Database connection failed: " + e.getMessage());
        }

        // Finalize Analytic Calculations
        long avgLatency = successfulLatencyCount > 0 ? (totalLatency / successfulLatencyCount) : 0;
        double successRate = totalRequests > 0 ? ((double) successfulRequests / totalRequests) * 100 : 0.0;

        // Determine the most popular query string
        String topQuery = "N/A";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : queryCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topQuery = entry.getKey();
            }
        }

        // Bind data attributes for the JSP view
        request.setAttribute("logs", logs);
        request.setAttribute("totalRequests", totalRequests);
        request.setAttribute("avgLatency", avgLatency);
        request.setAttribute("topQuery", topQuery);
        request.setAttribute("successRate", String.format("%.1f", successRate));
        request.setAttribute("totalPapersFetched", totalPapersFetched);

        // Forward execution to the JSP file
        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}