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

/**
 * Adaptateur permettant d'afficher les différentes œuvres d'un auteur.
 * Utilisé généralement dans un RecyclerView horizontal ou vertical sur la fiche Auteur.
 */
public class AuteurOeuvreAdapter extends RecyclerView.Adapter<AuteurOeuvreAdapter.ViewHolder> {

    // Liste des objets JSON représentant les mangas de l'auteur
    private List<JSONObject> listeOeuvres;

    /**
     * Constructeur pour initialiser la liste des œuvres.
     * @param listeOeuvres Liste de JSON contenant les infos des mangas (titre, image, éditeur)
     */
    public AuteurOeuvreAdapter(List<JSONObject> listeOeuvres) {
        this.listeOeuvres = listeOeuvres;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Chargement du layout XML spécifique pour un item de la bibliographie
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manga_auteur, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            // Récupération de l'objet JSON à la position actuelle
            JSONObject manga = listeOeuvres.get(position);

            // Extraction sécurisée des données du JSON
            String titreSerie = manga.optString("titre_serie", "Inconnu");
            String editeur = manga.optString("editeur_fr", "");
            String urlImage = manga.optString("image_url", "");

            // Mise à jour du texte (Titre de la série)
            if (holder.tvTitre != null) {
                holder.tvTitre.setText(titreSerie);
            }

            // Chargement asynchrone de l'image de couverture avec Glide
            if (holder.ivCouverture != null) {
                Glide.with(holder.itemView.getContext())
                        .load(urlImage)
                        .placeholder(R.drawable.placeholder_cover) // image par défaut pendant le chargement
                        .into(holder.ivCouverture);
            }

            // --- GESTION DU CLIC ET REDIRECTION ---
            // Lorsque l'utilisateur clique sur une œuvre, il est envoyé vers SerieActivity
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), SerieActivity.class);
                // Passage des données clés pour que la page Série sache quoi charger
                intent.putExtra("SERIE_NAME", titreSerie);
                intent.putExtra("EDITEUR_NAME", editeur);
                v.getContext().startActivity(intent);
            });

        } catch (Exception e) {
            Log.e("DEBUG_AUTEUR", "Erreur lors du lien des données : " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        // Retourne le nombre total d'œuvres à afficher
        return listeOeuvres.size();
    }

    /**
     * ViewHolder : Conteneur pour les vues d'un item.
     * Permet d'éviter de faire des findViewById répétitifs (gain de performance).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCouverture;
        TextView tvTitre;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Récupération des références vers les vues du layout item_manga_auteur
            ivCouverture = itemView.findViewById(R.id.iv_manga_auteur);
            tvTitre = itemView.findViewById(R.id.tv_titre_manga_auteur);
        }
    }
}