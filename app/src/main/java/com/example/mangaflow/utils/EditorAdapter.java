package com.example.mangaflow.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import java.util.List;

public class EditorAdapter extends RecyclerView.Adapter<EditorAdapter.EditorViewHolder> {

    private List<MangaClass> mangaList;

    // Constructeur qui accepte ta liste d'objets MangaClass
    public EditorAdapter(List<MangaClass> mangaList) {
        this.mangaList = mangaList;
    }

    @NonNull
    @Override
    public EditorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Utilise le layout de ligne que nous avons créé
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manga, parent, false);
        return new EditorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditorViewHolder holder, int position) {
        MangaClass manga = mangaList.get(position);

        // Affiche le titre via ton getter
        holder.mangaTitle.setText(manga.getTitre_serie());
    }

    @Override
    public int getItemCount() {
        return mangaList != null ? mangaList.size() : 0;
    }

    public static class EditorViewHolder extends RecyclerView.ViewHolder {
        TextView mangaTitle;

        public EditorViewHolder(@NonNull View itemView) {
            super(itemView);
            // Liaison avec l'ID du TextView dans item_manga.xml
            mangaTitle = itemView.findViewById(R.id.manga_title);
        }
    }
}