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
import com.example.mangaflow.activities.SerieActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

/**
 * Adaptateur polyvalent pour l'affichage des séries dans la bibliothèque.
 * Gère dynamiquement les barres de progression, les badges "Fini" et les compteurs de lecture.
 */
public class SerieAdapter extends RecyclerView.Adapter<SerieAdapter.ViewHolder> {
    private List<JSONObject> serieList;

    public SerieAdapter(List<JSONObject> serieList) { this.serieList = serieList; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflation du layout unique utilisé pour tous les modes de la bibliothèque
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manga_item_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject serie = serieList.get(position);
            String nomSerie = serie.optString("nom", "Série");
            holder.tvTitle.setText(nomSerie);

            // --- NAVIGATION ---
            // Envoi vers la page détaillée de la série au clic
            holder.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), SerieActivity.class);
                intent.putExtra("SERIE_NAME", nomSerie);
                intent.putExtra("EDITEUR_NAME", serie.optString("editeur_fr", ""));
                v.getContext().startActivity(intent);
            });

            // --- RÉINITIALISATION DE L'ÉTAT ---
            // Important car le RecyclerView réutilise les vues (évite les bugs d'affichage au scroll)
            holder.progressBar.setVisibility(View.GONE);
            holder.tvLabelFini.setVisibility(View.GONE);

            // --- LOGIQUE D'AFFICHAGE CONDITIONNELLE ---

            // Cas 1 : Mode Collection ou Séries à compléter
            if (serie.has("affichage_collection") || serie.has("is_completer_view")) {
                int total = serie.optInt("nombre_tome_total", 0);
                int possedes = serie.optInt("nb_possedes", 0);

                if (possedes >= total && total > 0) {
                    // Sous-cas : La série est complète (Possédé = Total)
                    holder.tvStatus.setText(total + " tomes");
                    holder.tvLabelFini.setVisibility(View.VISIBLE); // Affiche le badge "Fini"
                } else {
                    // Sous-cas : Série en cours d'acquisition
                    holder.tvStatus.setText(possedes + " / " + total + " tomes");
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.progressBar.setMax(total);
                    holder.progressBar.setProgress(possedes);
                }
            }
            // Cas 2 : Mode Wishlist (Souhaits)
            else if (serie.has("affichage_souhaiter")) {
                holder.tvStatus.setText(serie.optInt("nombre_tome_total") + " tomes");
            }
            // Cas 3 : Mode Pile à lire (Calculé dans un fragment dédié)
            else if (serie.has("nb_a_lire")) {
                int aLire = serie.optInt("nb_a_lire", 0);
                holder.tvStatus.setText(aLire > 1 ? aLire + " tomes à lire" : aLire + " tome à lire");
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#888888"));
            }

            // --- AFFICHAGE DE L'IMAGE ---
            // On récupère la jaquette du premier tome disponible dans la liste "mangas"
            JSONArray mangas = serie.optJSONArray("mangas");
            String imageUrl = (mangas != null && mangas.length() > 0) ? mangas.getJSONObject(0).optString("jaquette") : "";

            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_cover)
                    .into(holder.ivCover);

        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public int getItemCount() { return serieList.size(); }

    /**
     * ViewHolder : Fait le lien entre les variables Java et les composants du XML.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvStatus, tvLabelFini;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_manga_cover);
            tvTitle = itemView.findViewById(R.id.tv_manga_title);
            tvStatus = itemView.findViewById(R.id.tv_manga_info_status);
            tvLabelFini = itemView.findViewById(R.id.tv_label_fini);
            progressBar = itemView.findViewById(R.id.pb_manga_progress);
        }
    }
}