package com.example.mangaflow.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manga_item_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject serie = serieList.get(position);
            holder.tvTitle.setText(serie.optString("nom", "Série"));

            // 1. Initialisation par défaut (Cache la barre)
            holder.progressBar.setVisibility(View.GONE);

            // 2. Sélection du texte et du style
            if (serie.has("affichage_collection")) {
                // MODE COLLECTION
                int totalTomes = serie.optInt("nombre_tome_total", 0);
                int nbPossedes = serie.optInt("nb_possedes", 0);
                String statut = serie.optString("statut", "");

                String info = nbPossedes + " / " + totalTomes + " tomes";
                if (statut.toLowerCase().contains("cours")) info += " - Edition en cours";

                holder.tvStatus.setText(info);
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.progressBar.setMax(totalTomes);
                holder.progressBar.setProgress(nbPossedes);

            } else if (serie.has("nb_a_lire")) {
                // MODE A LIRE
                holder.tvStatus.setText(serie.getInt("nb_a_lire") + " tomes");

            } else if (serie.has("nb_manquant")) {
                // MODE COMPLÉTER
                holder.tvStatus.setText(serie.getInt("nb_manquant") + " tomes");

            } else if (serie.has("affichage_souhaiter")) {
                // MODE SOUHAITER
                int total = serie.optInt("nombre_tome_total", 0);
                holder.tvStatus.setText(total + " tomes");
            }

            // Image
            JSONArray mangas = serie.optJSONArray("mangas");
            String imageUrl = (mangas != null && mangas.length() > 0)
                    ? mangas.getJSONObject(0).optString("jaquette") : "";

            Glide.with(holder.itemView.getContext()).load(imageUrl).into(holder.ivCover);

        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public int getItemCount() { return serieList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvStatus;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_manga_cover);
            tvTitle = itemView.findViewById(R.id.tv_manga_title);
            tvStatus = itemView.findViewById(R.id.tv_manga_info_status);
            progressBar = itemView.findViewById(R.id.pb_manga_progress);
        }
    }
}