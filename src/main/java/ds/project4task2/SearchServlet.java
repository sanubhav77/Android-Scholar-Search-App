/**
 * SearchServlet.java
 *
 * The primary web service endpoint for the ScholarSearch application.
 * Accepts HTTP GET requests containing a search query, communicates with the
 * 3rd-party arXiv API via ArxivClient, logs the operation metrics via MongoLogger,
 * and returns a minimal JSON payload to the Android client.
 *
 * Thank you to arXiv for use of its open access interoperability.
 *
 * Author: Anubhav Sharma
 * AndrewID: anubhav3
 */
package ds.project4task2;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Enforce JSON output and UTF-8 encoding
        response.setContentType("application/json; charset=UTF-8");

        // Retrieve query parameters
        String query = request.getParameter("query");
        String maxResultsParam = request.getParameter("maxResults");

        // Determine the maximum number of results, enforcing a hard cap of 25
        int maxResults = 5;
        if (maxResultsParam != null && !maxResultsParam.trim().isEmpty()) {
            try {
                maxResults = Integer.parseInt(maxResultsParam);
                if (maxResults > 25) maxResults = 25;
                if (maxResults < 1) maxResults = 5;
            } catch (NumberFormatException e) {
                // Ignore parse errors and fall back to the default of 5
            }
        }

        // Capture client metadata for MongoDB logging
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) userAgent = "Unknown";

        // Capture IP, checking the X-Forwarded-For header in case of proxies (like Docker)
        String clientIP = request.getHeader("X-Forwarded-For");
        if (clientIP == null || clientIP.isEmpty()) {
            clientIP = request.getRemoteAddr();
        }

        // Initialize state variables for logging
        long timestamp = System.currentTimeMillis();
        long latencyMs = -1;
        int resultCount = 0;
        boolean success = false;
        int httpStatus = HttpServletResponse.SC_OK;

        JSONObject jsonResponse = new JSONObject();

        try (PrintWriter out = response.getWriter()) {
            // Validate input: ensure query is not null or empty
            if (query == null || query.trim().isEmpty()) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST; // 400
                response.setStatus(httpStatus);
                jsonResponse.put("error", "Missing or empty 'query' parameter.");
                out.print(jsonResponse.toString());
                return; // Exit early, but 'finally' block will still trigger the logger
            }

            // Record start time to calculate 3rd-party API latency
            long startTime = System.currentTimeMillis();

            ArxivClient client = new ArxivClient();
            List<Paper> papers;
            try {
                // Execute the external API call
                papers = client.search(query, maxResults);
            } catch (Exception e) {
                // Handle failures from the arXiv API
                httpStatus = HttpServletResponse.SC_BAD_GATEWAY; // 502
                response.setStatus(httpStatus);
                jsonResponse.put("error", "Failed to fetch data from arXiv API.");
                jsonResponse.put("details", e.getMessage());
                out.print(jsonResponse.toString());
                return;
            }

            // Calculate latency and update success metrics
            latencyMs = System.currentTimeMillis() - startTime;
            resultCount = papers.size();
            success = true;

            // Construct the minimal JSON response required by the Android client
            jsonResponse.put("query", query);
            jsonResponse.put("count", resultCount);

            JSONArray jsonPapers = new JSONArray();
            for (Paper p : papers) {
                JSONObject paperJson = new JSONObject();
                paperJson.put("title", p.getTitle() != null ? p.getTitle() : "");
                paperJson.put("authors", p.getAuthors() != null ? p.getAuthors() : "");
                paperJson.put("published", p.getPublished() != null ? p.getPublished() : "");
                paperJson.put("categories", p.getCategories() != null ? p.getCategories() : "");
                paperJson.put("summary", p.getSummary() != null ? p.getSummary() : "");
                paperJson.put("pdfUrl", p.getPdfUrl() != null ? p.getPdfUrl() : "");
                paperJson.put("arxivId", p.getArxivId() != null ? p.getArxivId() : "");

                jsonPapers.put(paperJson);
            }
            jsonResponse.put("papers", jsonPapers);

            // Transmit the successful JSON payload
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            // Catch any unexpected server-side errors
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR; // 500
            response.setStatus(httpStatus);
            try {
                JSONObject errorJson = new JSONObject();
                errorJson.put("error", "Internal Server Error");
                response.getWriter().print(errorJson.toString());
            } catch (Exception ignored) {}
        } finally {
            // Guaranteed execution: Log the request to MongoDB Atlas regardless of outcome
            try {
                MongoLogger.logRequest(
                        timestamp,
                        query != null ? query : "null",
                        userAgent,
                        clientIP,
                        latencyMs,
                        resultCount,
                        success,
                        httpStatus
                );
            } catch (Exception logEx) {
                System.err.println("Failed to log request to MongoDB: " + logEx.getMessage());
            }
        }
    }
}