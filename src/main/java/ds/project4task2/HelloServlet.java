
/**
 * This class implements a basic HTTP Servlet that responds to GET requests
 * with a simple HTML "Hello World!" message. It serves as a foundational
 * testing endpoint to verify that the web application and Servlet container
 * (Tomcat) are deployed and functioning correctly in the cloud environment.
 *
 * Author: Anubhav Sharma
 * Andrew ID: anubhav3
 */

package ds.project4task2;

import java.io.*;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "RootServlet", urlPatterns = {"", "/"})
public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.getWriter().println(
                "<!DOCTYPE html><html><head><title>ScholarSearch</title>" +
                        "<style>body{font-family:sans-serif;max-width:700px;margin:40px auto;padding:20px}" +
                        "h1{color:#005A9C}code{background:#f0f0f0;padding:2px 6px;border-radius:3px}</style>" +
                        "</head><body>" +
                        "<h1>ScholarSearch Web Service</h1>" +
                        "<p>95-702 Distributed Systems, Project 4 Task 2 by Anubhav Sharma (anubhav3).</p>" +
                        "<p>Endpoints:</p>" +
                        "<ul>" +
                        "<li><a href='/search?query=quantum+computing'>/search?query=quantum+computing</a> — arXiv paper search (JSON)</li>" +
                        "<li><a href='/dashboard'>/dashboard</a> — operations dashboard</li>" +
                        "</ul>" +
                        "</body></html>"
        );
    }
}