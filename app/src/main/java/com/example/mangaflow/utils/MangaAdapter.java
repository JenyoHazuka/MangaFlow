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
import com.example.mangaflow.models.MangaClass;

import java.util.List;

/**
 * Adaptateur universel pour afficher des tomes de manga.
 * Il accepte différents layouts (ex: item_manga_home ou item_manga_carrousel)
 * pour s'adapter au contexte d'affichage.
 */
public class MangaAdapter extends RecyclerView.Adapter<MangaAdapter.MangaViewHolder> {

    private List<MangaClass> mangaList;
    private int layoutId; // Identifiant du layout XML à utiliser (R.layout.xxx)

    /**
     * Constructeur
     * @param mangaList Liste des objets MangaClass à afficher
     * @param layoutId Le fichier XML de l'item (permet la réutilisabilité)
     */
    public MangaAdapter(List<MangaClass> mangaList, int layoutId) {
        this.mangaList = mangaList;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public MangaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflation du layout passé au constructeur
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MangaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MangaViewHolder holder, int position) {
        MangaClass manga = mangaList.get(position);

        // --- SÉCURITÉ AFFICHAGE ---
        // On vérifie la présence des vues avant de les remplir car certains layouts
        // (comme le carrousel) peuvent ne pas posséder tous les TextView.

        if (holder.title != null) {
            holder.title.setText(manga.getTitre_serie());
        }

        if (holder.tome != null) {
            holder.tome.setText("Tome " + manga.getNumero_tome());
        }

        // Chargement de la jaquette avec Glide
        if (holder.image != null) {
            Glide.with(holder.itemView.getContext())
                    .load(manga.getImage_url())
                    .placeholder(R.drawable.placeholder_cover) // Image pendant le chargement
                    .error(R.drawable.placeholder_cover)       // Image en cas d'erreur
                    .into(holder.image);
        }

        // --- GESTION DU CLIC ---
        // Redirige vers MangaActivity en passant les identifiants uniques (Titre + Numéro)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MangaActivity.class);
            try {
                intent.putExtra("TITRE_MANGA", manga.getTitre_serie());
                intent.putExtra("NUMERO_TOME", manga.getNumero_tome());
                v.getContext().startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mangaList.size();
    }

    /**
     * ViewHolder optimisé pour la flexibilité des layouts.
     */
    public static class MangaViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, tome;

        public MangaViewHolder(@NonNull View itemView) {
            super(itemView);
            // On récupère les IDs. Si un ID n'existe pas dans le layout actuel,
            // la variable vaudra simplement 'null', d'où l'importance des tests dans onBindViewHolder.
            image = itemView.findViewById(R.id.manga_image);
            title = itemView.findViewById(R.id.manga_title);
            tome = itemView.findViewById(R.id.manga_tome);
        }
    }
}