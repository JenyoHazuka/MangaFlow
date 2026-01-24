package com.example.mangaflow.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mangaflow.R;

import java.util.List;

public class MangaAdapter extends RecyclerView.Adapter<MangaAdapter.MangaViewHolder> {

    private List<MangaClass> mangaList;

    public MangaAdapter(List<MangaClass> mangaList) {
        this.mangaList = mangaList;
    }

    @NonNull
    @Override
    public MangaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // On transforme le XML manga_item en un objet Java
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manga_item, parent, false);
        return new MangaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MangaViewHolder holder, int position) {
        // On récupère les données du manga à cette position
        MangaClass manga = mangaList.get(position);

        // On remplit le texte
        holder.title.setText(manga.getTitre_serie());
        holder.tome.setText("Tome " + manga.getNumero_tome());

        // On charge l'image avec Glide (URL -> ImageView)
        Glide.with(holder.itemView.getContext())
                .load(manga.getImage_url())
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return mangaList.size();
    }

    // Cette classe interne fait le lien avec les IDs du XML manga_item
    static class MangaViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, tome;

        public MangaViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.manga_image);
            title = itemView.findViewById(R.id.manga_title);
            tome = itemView.findViewById(R.id.manga_tome);
        }
    }
}