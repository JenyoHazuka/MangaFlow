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

/**
 * Adaptateur multi-vues pour la page Éditeur.
 * Il permet d'afficher des en-têtes (ex: "Mangas", "Artbooks") et des noms de séries
 * dans un seul flux continu.
 */
public class EditorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<EditorItem> items;

    public EditorAdapter(List<EditorItem> items) {
        this.items = items;
    }

    /**
     * Détermine quel type de layout utiliser pour une position donnée.
     * Renvoie TYPE_HEADER (0) ou TYPE_MANGA (1).
     */
    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == EditorItem.TYPE_HEADER) {
            // Inflation du layout pour les titres de sections
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_editor_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            // Inflation du layout pour les lignes de mangas individuelles
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manga, parent, false);
            return new MangaViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EditorItem item = items.get(position);

        // Logique conditionnelle selon le type de ViewHolder détecté
        if (holder instanceof HeaderViewHolder) {
            // Configuration de l'en-tête (Simple texte)
            ((HeaderViewHolder) holder).tvSection.setText(item.title);
        } else {
            // Configuration de l'item manga et gestion du clic
            MangaViewHolder mHolder = (MangaViewHolder) holder;
            mHolder.tvTitle.setText(item.title);

            mHolder.itemView.setOnClickListener(v -> {
                // Redirection vers la page de la série au clic
                Intent intent = new Intent(v.getContext(), SerieActivity.class);
                intent.putExtra("SERIE_NAME", item.title);
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // --- VIEW HOLDERS ---

    /**
     * Conteneur pour les en-têtes de sections.
     */
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSection;
        public HeaderViewHolder(View v) {
            super(v);
            tvSection = v.findViewById(R.id.section_title);
        }
    }

    /**
     * Conteneur pour les items de mangas cliquables.
     */
    public static class MangaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        public MangaViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.manga_title);
        }
    }
}