/**
 * PaperAdapter.java
 * RecyclerView adapter that binds Paper objects to item_paper.xml rows.
 * Clicking a row opens the PDF URL in the browser.
 * Author: Anubhav Sharma
 * AndrewID: anubhav3
 */
package com.example.project4task2android;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaperAdapter extends RecyclerView.Adapter<PaperAdapter.PaperViewHolder> {

    private final List<Paper> papers;

    public PaperAdapter(List<Paper> papers) {
        this.papers = papers;
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paper, parent, false);
        return new PaperViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder h, int position) {
        Paper p = papers.get(position);
        h.title.setText(p.title);
        h.authors.setText(p.authors);
        // Show published date (first 10 chars = YYYY-MM-DD) + categories
        String date = p.published != null && p.published.length() >= 10
                ? p.published.substring(0, 10) : p.published;
        h.meta.setText(date + "  •  " + p.categories);
        h.summary.setText(p.summary);

        // Click a row to open the PDF in the browser
        h.itemView.setOnClickListener(view -> {
            if (p.pdfUrl != null && !p.pdfUrl.isEmpty()) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(p.pdfUrl));
                view.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() { return papers.size(); }

    static class PaperViewHolder extends RecyclerView.ViewHolder {
        TextView title, authors, meta, summary;
        PaperViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.paperTitle);
            authors = v.findViewById(R.id.paperAuthors);
            meta = v.findViewById(R.id.paperMeta);
            summary = v.findViewById(R.id.paperSummary);
        }
    }
}