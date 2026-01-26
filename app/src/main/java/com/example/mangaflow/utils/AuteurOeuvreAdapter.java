package com.example.mangaflow.utils;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mangaflow.R;
import com.example.mangaflow.activities.SerieActivity;

import org.json.JSONObject;
import java.util.List;

public class AuteurOeuvreAdapter extends RecyclerView.Adapter<AuteurOeuvreAdapter.ViewHolder> {

    // Voici la variable qui vous manquait
    private List<JSONObject> listeOeuvres;

    // Le constructeur pour recevoir les données depuis l'activité
    public AuteurOeuvreAdapter(List<JSONObject> listeOeuvres) {
        this.listeOeuvres = listeOeuvres;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // On gonfle le layout item_manga_auteur que nous venons de créer
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manga_auteur, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject manga = listeOeuvres.get(position);

            // Récupération des données du JSON
            String titreSerie = manga.optString("titre_serie", "Inconnu");
            String editeur = manga.optString("editeur_fr", "");
            String urlImage = manga.optString("image_url", "");

            // Affichage sécurisé
            if (holder.tvTitre != null) {
                holder.tvTitre.setText(titreSerie);
            }

            if (holder.ivCouverture != null) {
                Glide.with(holder.itemView.getContext())
                        .load(urlImage)
                        .into(holder.ivCouverture);
            }

            // --- REDIRECTION VERS LA PAGE SÉRIE ---
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), SerieActivity.class);
                // On passe les infos nécessaires à SerieActivity
                intent.putExtra("SERIE_NAME", titreSerie);
                intent.putExtra("EDITEUR_NAME", editeur);
                v.getContext().startActivity(intent);
            });

        } catch (Exception e) {
            Log.e("DEBUG_AUTEUR", "Erreur clic : " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return listeOeuvres.size();
    }

    // Le ViewHolder fait le lien avec les IDs du XML item_manga_auteur
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCouverture;
        TextView tvTitre;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCouverture = itemView.findViewById(R.id.iv_manga_auteur);
            tvTitre = itemView.findViewById(R.id.tv_titre_manga_auteur);
        }
    }
}