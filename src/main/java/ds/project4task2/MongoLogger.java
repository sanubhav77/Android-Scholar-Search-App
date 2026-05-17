/**
 * MongoLogger.java
 *
 * A utility class responsible for logging all incoming search requests to a cloud-based
 * MongoDB Atlas cluster. It captures essential metadata including timestamps, client IP,
 * user agent, API latency, and HTTP status codes to facilitate dashboard analytics.
 *
 * Thank you to arXiv for use of its open access interoperability.
 *
 * Author: Anubhav Sharma
 * AndrewID: anubhav3
 */
package ds.project4task2;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Date;

public class MongoLogger {

    // Connection string for the MongoDB Atlas cluster
    // Paste actual MongoDB Atlas connection string here.
   private static final String CONNECTION_STRING = "mongodb+srv://anubhav3_db_user:REPLACE_WITH_YOUR_KEY@cluster0.24nur6o.mongodb.net/?appName=Cluster0";

    // Database and Collection identifiers
    private static final String DB_NAME = "ScholarSearchDB";
    private static final String COLLECTION_NAME = "SearchLogs";

    /**
     * Logs request analytics to MongoDB.
     * Captures 8 distinct pieces of information to satisfy the project requirements.
     *
     * @param timestamp The exact time the request was received.
     * @param query The search term provided by the user.
     * @param userAgent Information about the device/browser making the request.
     * @param clientIP The IP address of the client making the request.
     * @param arxivLatencyMs The time taken in milliseconds to fetch data from arXiv.
     * @param resultCount The total number of papers successfully returned.
     * @param success Boolean indicating if the entire operation was successful.
     * @param httpStatus The final HTTP status code returned to the client.
     */
    public static void logRequest(long timestamp, String query, String userAgent,
                                  String clientIP, long arxivLatencyMs, int resultCount,
                                  boolean success, int httpStatus) {

        // Use try-with-resources to ensure the MongoClient connection is closed safely after use.
        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DB_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Construct the BSON Document using the provided parameters
            Document logEntry = new Document("timestamp", new Date(timestamp))
                    .append("query", query)
                    .append("userAgent", userAgent)
                    .append("clientIP", clientIP)
                    .append("arxivLatencyMs", arxivLatencyMs)
                    .append("resultCount", resultCount)
                    .append("success", success)
                    .append("httpStatus", httpStatus);

            // Insert the generated document into the Atlas collection
            collection.insertOne(logEntry);
            System.out.println("Successfully logged request to MongoDB Atlas.");

        } catch (Exception e) {
            // Catch exceptions so a database error does not crash the servlet
            System.err.println("MongoDB Logging Error: " + e.getMessage());
        }
    }
}