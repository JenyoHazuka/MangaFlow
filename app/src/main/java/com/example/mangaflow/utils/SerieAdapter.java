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
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class SerieAdapter extends RecyclerView.Adapter<SerieAdapter.ViewHolder> {

    private List<JSONObject> serieList;

    public SerieAdapter(List<JSONObject> serieList) {
        this.serieList = serieList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // parent.getContext() est utilisé ici pour gonfler le layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_serie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject serie = serieList.get(position);

            // Affichage du titre de la série
            holder.tvTitle.setText(serie.optString("nom", "Série inconnue"));

            // RÉCUPÉRATION SÉCURISÉE DE LA JAQUETTE
            String imageUrl = "";
            JSONArray mangas = serie.optJSONArray("mangas");

            if (mangas != null && mangas.length() > 0) {
                // On prend la jaquette du premier tome disponible dans la liste
                imageUrl = mangas.getJSONObject(0).optString("jaquette", "");
            }

            // Chargement de l'image avec Glide
            // holder.itemView.getContext() récupère le contexte nécessaire à Glide
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_cover) // Image pendant le chargement
                    .error(R.drawable.placeholder_cover)       // Image si l'URL est vide ou erronée
                    .centerCrop()
                    .into(holder.ivCover);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return serieList != null ? serieList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_item_cover);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
        }
    }
}