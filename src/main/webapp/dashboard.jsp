<%--
 /**
  * dashboard.jsp
  *
  * The presentation layer for the ScholarSearch operations dashboard.
  * Displays five distinct analytics and renders a formatted HTML table of all
  * logged requests, avoiding the penalty for displaying raw JSON or XML.
  *
  * Thank you to arXiv for use of its open access interoperability.
  *
  * Author: Anubhav Sharma
  * AndrewID: anubhav3
  */
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.bson.Document" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html>
<head>
    <title>ScholarSearch Operations Dashboard</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 30px; color: #333; }
        h1 { color: #005A9C; border-bottom: 2px solid #005A9C; padding-bottom: 10px; }
        .analytics-panel { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 6px; padding: 20px; margin-bottom: 30px; }
        .analytics-panel h2 { margin-top: 0; color: #495057; font-size: 1.4em; }
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-top: 15px; }
        .stat-box { background: white; padding: 15px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); border-left: 4px solid #005A9C; }
        .stat-label { font-size: 0.9em; color: #666; text-transform: uppercase; letter-spacing: 0.5px; }
        .stat-value { font-size: 1.5em; font-weight: bold; color: #005A9C; margin-top: 5px; }
        table { width: 100%; border-collapse: collapse; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
        th, td { border: 1px solid #e9ecef; padding: 12px; text-align: left; }
        th { background-color: #005A9C; color: white; font-weight: 500; }
        tr:nth-child(even) { background-color: #f8f9fa; }
        tr:hover { background-color: #e9ecef; }
        .error { color: #dc3545; font-weight: bold; }
    </style>
</head>
<body>

<h1>ScholarSearch Operations Dashboard</h1>

<%-- Render any database connection errors passed from the Servlet --%>
<%
    String errorMsg = (String) request.getAttribute("error");
    if (errorMsg != null) {
        out.println("<p class='error'>" + errorMsg + "</p>");
    }
%>

<%-- 5 Analytics Section --%>
<div class="analytics-panel">
    <h2>Live Analytics</h2>
    <div class="stats-grid">
        <div class="stat-box">
            <div class="stat-label">Total API Requests</div>
            <div class="stat-value"><%= request.getAttribute("totalRequests") %></div>
        </div>
        <div class="stat-box">
            <div class="stat-label">Avg. arXiv Latency</div>
            <div class="stat-value"><%= request.getAttribute("avgLatency") %> ms</div>
        </div>
        <div class="stat-box">
            <div class="stat-label">Most Frequent Search</div>
            <div class="stat-value">"<%= request.getAttribute("topQuery") %>"</div>
        </div>
        <div class="stat-box">
            <div class="stat-label">API Success Rate</div>
            <div class="stat-value"><%= request.getAttribute("successRate") %>%</div>
        </div>
        <div class="stat-box">
            <div class="stat-label">Total Papers Fetched</div>
            <div class="stat-value"><%= request.getAttribute("totalPapersFetched") %></div>
        </div>
    </div>
</div>

<%-- Formatted Request Logs Table --%>
<h2>Request Logs</h2>
<table>
    <thead>
    <tr>
        <th>Timestamp</th>
        <th>Query</th>
        <th>Results</th>
        <th>Latency (ms)</th>
        <th>HTTP Status</th>
        <th>Client IP</th>
        <th>User Agent</th>
    </tr>
    </thead>
    <tbody>
    <%
        List<Document> logs = (List<Document>) request.getAttribute("logs");
        if (logs != null && !logs.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // Iterate through each MongoDB document and extract values
            for (Document doc : logs) {
                Date ts = doc.getDate("timestamp");
                String formattedDate = (ts != null) ? sdf.format(ts) : "N/A";
                boolean isSuccess = doc.getBoolean("success", false);
                String statusStr = isSuccess ? "Success (" + doc.getInteger("httpStatus", 200) + ")" : "Failed (" + doc.getInteger("httpStatus", 500) + ")";
    %>
    <tr>
        <td><%= formattedDate %></td>
        <td><strong><%= doc.getString("query") %></strong></td>
        <td><%= doc.getInteger("resultCount", 0) %></td>
        <td><%= doc.getLong("arxivLatencyMs") %></td>
        <td><%= statusStr %></td>
        <td><%= doc.getString("clientIP") %></td>
        <td style="font-size: 0.85em; color: #666;"><%= doc.getString("userAgent") %></td>
    </tr>
    <%
        }
    } else {
    %>
    <tr>
        <td colspan="7" style="text-align: center; padding: 20px;">No logs found in database.</td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>

</body>
</html>