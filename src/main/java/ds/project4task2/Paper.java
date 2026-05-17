/**
 * Paper.java
 *
 * Data model representing a single academic paper fetched from the arXiv API.
 * Used by ArxivClient to return structured results and by SearchServlet
 * to build the JSON response sent back to the Android app.
 *
 * Author: Anubhav Sharma
 * AndrewID: anubhav3
 */
package ds.project4task2;

public class Paper {

    private final String title;
    private final String authors;      // comma-separated list
    private final String published;    // ISO date string
    private final String categories;   // comma-separated list of arXiv categories
    private final String summary;      // abstract (may be truncated)
    private final String pdfUrl;
    private final String arxivId;      // arXiv URL (e.g. http://arxiv.org/abs/2501.12345)

    public Paper(String title, String authors, String published,
                 String categories, String summary, String pdfUrl, String arxivId) {
        this.title = title;
        this.authors = authors;
        this.published = published;
        this.categories = categories;
        this.summary = summary;
        this.pdfUrl = pdfUrl;
        this.arxivId = arxivId;
    }

    public String getTitle() { return title; }
    public String getAuthors() { return authors; }
    public String getPublished() { return published; }
    public String getCategories() { return categories; }
    public String getSummary() { return summary; }
    public String getPdfUrl() { return pdfUrl; }
    public String getArxivId() { return arxivId; }
}