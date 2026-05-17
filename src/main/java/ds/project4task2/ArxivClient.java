/**
 * ArxivClient.java
 *
 * Client for the arXiv public API (https://info.arxiv.org/help/api/index.html).
 * Fetches papers matching a search query, parses the Atom XML response,
 * and returns a list of Paper objects. Used by SearchServlet to fulfill
 * requests from the ScholarSearch Android application.
 *
 * Thank you to arXiv for use of its open access interoperability.
 *
 * Author: Anubhav Sharma
 * AndrewID: anubhav3
 */
package ds.project4task2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ArxivClient {

    private static final String ARXIV_API_URL = "https://export.arxiv.org/api/query";
    private static final String ATOM_NS = "http://www.w3.org/2005/Atom";

    /**
     * Searches arXiv for papers matching the given query string.
     *
     * @param query      the search term (will be URL-encoded)
     * @param maxResults maximum number of papers to return (capped at 25)
     * @return a list of Paper objects (possibly empty if no matches)
     * @throws Exception if the API is unreachable or the response cannot be parsed
     */
    public List<Paper> search(String query, int maxResults) throws Exception {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }
        if (maxResults < 1) maxResults = 5;
        if (maxResults > 25) maxResults = 25;

        // Build request URL - "all:" searches across title, author, abstract, etc.
        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String queryUrl = ARXIV_API_URL
                + "?search_query=all:" + encodedQuery
                + "&start=0"
                + "&max_results=" + maxResults
                + "&sortBy=submittedDate"
                + "&sortOrder=descending";

        // Make the HTTP GET request
        URL url = new URL(queryUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "ScholarSearch/1.0");
        connection.setConnectTimeout(10000);  // 10 seconds
        connection.setReadTimeout(10000);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            connection.disconnect();
            throw new RuntimeException("arXiv API returned HTTP " + responseCode);
        }

        // Parse the Atom XML response
        List<Paper> papers = new ArrayList<>();
        try (InputStream inputStream = connection.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList entries = doc.getElementsByTagNameNS(ATOM_NS, "entry");
            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);
                papers.add(parseEntry(entry));
            }
        } finally {
            connection.disconnect();
        }

        return papers;
    }

    /**
     * Parses a single <entry> element from the arXiv Atom feed into a Paper object.
     */
    private Paper parseEntry(Element entry) {
        // Title (arXiv wraps titles with newlines - clean them up)
        String title = getText(entry, "title").replaceAll("\\s+", " ").trim();

        // Authors - concatenate all <author><name> children
        NodeList authorNodes = entry.getElementsByTagNameNS(ATOM_NS, "author");
        StringBuilder authors = new StringBuilder();
        for (int j = 0; j < authorNodes.getLength(); j++) {
            Element authorElem = (Element) authorNodes.item(j);
            String name = getText(authorElem, "name");
            if (j > 0) authors.append(", ");
            authors.append(name);
        }

        // Published date
        String published = getText(entry, "published");

        // Categories - from <category term="..."/> attributes
        NodeList categoryNodes = entry.getElementsByTagNameNS(ATOM_NS, "category");
        StringBuilder categories = new StringBuilder();
        for (int j = 0; j < categoryNodes.getLength(); j++) {
            Element catElem = (Element) categoryNodes.item(j);
            String term = catElem.getAttribute("term");
            if (j > 0) categories.append(", ");
            categories.append(term);
        }

        // Summary (abstract) - trim to 300 chars so mobile response stays small
        String summary = getText(entry, "summary").replaceAll("\\s+", " ").trim();
        if (summary.length() > 300) {
            summary = summary.substring(0, 300) + "...";
        }

        // PDF link - find the <link> element with type="application/pdf"
        String pdfUrl = "";
        NodeList linkNodes = entry.getElementsByTagNameNS(ATOM_NS, "link");
        for (int j = 0; j < linkNodes.getLength(); j++) {
            Element linkElem = (Element) linkNodes.item(j);
            if ("application/pdf".equals(linkElem.getAttribute("type"))) {
                pdfUrl = linkElem.getAttribute("href");
                break;
            }
        }

        // arXiv URL (the <id> element)
        String arxivId = getText(entry, "id");

        return new Paper(title, authors.toString(), published,
                categories.toString(), summary, pdfUrl, arxivId);
    }

    /**
     * Helper: get text content of the first child with the given local name in the Atom namespace.
     */
    private String getText(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(ATOM_NS, localName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }
}