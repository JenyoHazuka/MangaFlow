package com.example.mangaflow.utils;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.activities.SerieActivity;
import com.example.mangaflow.models.EditorItem;

import java.util.List;

public class EditorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<EditorItem> items;

    public EditorAdapter(List<EditorItem> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == EditorItem.TYPE_HEADER) {
            // Chargement du layout d'entête
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_editor_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            // CORRECTION ICI : Utilisation de R.layout au lieu de R.id
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manga, parent, false);
            return new MangaViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EditorItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvSection.setText(item.title);
        } else {
            MangaViewHolder mHolder = (MangaViewHolder) holder;
            mHolder.tvTitle.setText(item.title);

            mHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), SerieActivity.class);
                intent.putExtra("SERIE_NAME", item.title);
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSection;
        public HeaderViewHolder(View v) { super(v); tvSection = v.findViewById(R.id.section_title); }
    }

    public static class MangaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        public MangaViewHolder(View v) { super(v); tvTitle = v.findViewById(R.id.manga_title); }
    }
}