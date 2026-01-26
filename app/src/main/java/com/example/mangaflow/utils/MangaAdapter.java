package com.example.mangaflow.utils;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mangaflow.R;
import com.example.mangaflow.activities.MangaActivity;
import java.util.List;

public class MangaAdapter extends RecyclerView.Adapter<MangaAdapter.MangaViewHolder> {

    private List<MangaClass> mangaList;
    private int layoutId;

    public MangaAdapter(List<MangaClass> mangaList, int layoutId) {
        this.mangaList = mangaList;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public MangaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MangaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MangaViewHolder holder, int position) {
        MangaClass manga = mangaList.get(position);

        // --- SÉCURITÉ TITRE ---
        // On ne remplit le titre que s'il existe dans le layout (Home)
        if (holder.title != null) {
            holder.title.setText(manga.getTitre_serie());
        }

        // --- SÉCURITÉ TOME ---
        if (holder.tome != null) {
            holder.tome.setText("Tome " + manga.getNumero_tome());
        }

        // Chargement de l'image (Présente dans les deux layouts)
        if (holder.image != null) {
            Glide.with(holder.itemView.getContext())
                    .load(manga.getImage_url())
                    .placeholder(R.drawable.placeholder_cover)
                    .error(R.drawable.placeholder_cover)
                    .into(holder.image);
        }

        // CLIC SUR L'ITEM
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MangaActivity.class);
            try {
                // On passe les DEUX informations cruciales
                intent.putExtra("TITRE_MANGA", manga.getTitre_serie());
                intent.putExtra("NUMERO_TOME", manga.getNumero_tome());
                v.getContext().startActivity(intent);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    @Override
    public int getItemCount() {
        return mangaList.size();
    }

    public static class MangaViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, tome;

        public MangaViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ces findViewById peuvent renvoyer null selon le layout utilisé
            image = itemView.findViewById(R.id.manga_image);
            title = itemView.findViewById(R.id.manga_title); // Sera null dans le carrousel
            tome = itemView.findViewById(R.id.manga_tome);
        }
    }
}