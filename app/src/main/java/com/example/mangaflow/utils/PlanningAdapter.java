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
import org.json.JSONObject;
import java.util.List;

/**
 * Adaptateur spécialisé pour le planning des sorties.
 * Gère deux types de vues : les en-têtes de date (String) et les fiches mangas (JSONObject).
 */
public class PlanningAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // Définition des constantes pour identifier le type de vue
    private static final int TYPE_HEADER = 0, TYPE_MANGA = 1;

    // Liste générique contenant des Strings et des JSONObjects mélangés
    private List<Object> itemList;

    public PlanningAdapter(List<Object> itemList) { this.itemList = itemList; }

    /**
     * Analyse l'objet à la position donnée pour déterminer s'il s'agit d'une Date ou d'un Manga.
     */
    @Override
    public int getItemViewType(int position) {
        return (itemList.get(position) instanceof String) ? TYPE_HEADER : TYPE_MANGA;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Selon le type détecté, on gonfle (inflate) le layout correspondant
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_planning_header, parent, false));
        }
        return new MangaVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manga_planning, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // CAS 1 : C'est un en-tête (Date)
        if (getItemViewType(position) == TYPE_HEADER) {
            HeaderVH h = (HeaderVH) holder;
            h.tvDate.setText((String) itemList.get(position));
        }
        // CAS 2 : C'est un item Manga
        else {
            JSONObject manga = (JSONObject) itemList.get(position);
            MangaVH h = (MangaVH) holder;

            String titreSerie = manga.optString("titre_serie", "Sans titre");
            h.tvTitle.setText(titreSerie);
            h.tvTome.setText("Tome " + manga.optString("numero_tome", "X"));

            // Chargement de l'image de couverture optimisé avec Glide
            Glide.with(h.itemView.getContext())
                    .load(manga.optString("image_url"))
                    .placeholder(R.drawable.placeholder_cover)
                    .centerCrop() // Recadre l'image pour remplir le cadre harmonieusement
                    .into(h.ivCover);

            // --- GESTION DU CLIC ---
            // Redirige l'utilisateur vers la page de la série correspondante
            h.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.mangaflow.activities.SerieActivity.class);
                // On passe le nom de la série en paramètre
                intent.putExtra("SERIE_NAME", titreSerie);
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() { return itemList.size(); }

    // --- VIEW HOLDERS ---

    /**
     * ViewHolder pour les séparateurs de dates.
     */
    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvDate;
        HeaderVH(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tv_header_date);
        }
    }

    /**
     * ViewHolder pour les fiches mangas du planning.
     */
    static class MangaVH extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvTome;
        MangaVH(View v) {
            super(v);
            ivCover = v.findViewById(R.id.iv_planning_cover);
            tvTitle = v.findViewById(R.id.tv_planning_title);
            tvTome = v.findViewById(R.id.tv_planning_tome);
        }
    }
}