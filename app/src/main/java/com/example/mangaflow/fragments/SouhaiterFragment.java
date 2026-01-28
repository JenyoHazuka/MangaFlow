package com.example.mangaflow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.activities.CollectionActivity;
import com.example.mangaflow.utils.SerieAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment gérant la liste d'envies (Wishlist).
 * Affiche les séries suivies pour lesquelles l'utilisateur ne possède encore aucun tome.
 */
public class SouhaiterFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. Initialisation de l'interface
        View view = inflater.inflate(R.layout.fragment_souhaiter, container, false);

        // 2. Récupération des données partagées par CollectionActivity
        CollectionActivity activity = (CollectionActivity) getActivity();
        if (activity == null) return view;

        JSONArray collection = activity.getCollection(); // Données de l'utilisateur
        JSONArray seriesRef = activity.getSeriesReference(); // Référentiel global

        TextView tvSouhaits = view.findViewById(R.id.tv_souhaiter_title);
        RecyclerView rv = view.findViewById(R.id.rv_souhaiter);

        int nbTomesSouhaiter = 0; // Compteur global des tomes attendus
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            // 3. LOGIQUE DE FILTRAGE ET DE JOINTURE
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                String nomSerie = serie.optString("nom");

                // Étape A : On récupère les infos globales (Total et Éditeur) pour l'affichage
                int totalTheorique = 0;
                String editeurRef = "";
                for (int j = 0; j < seriesRef.length(); j++) {
                    JSONObject ref = seriesRef.getJSONObject(j);
                    if (ref.optString("titre").equalsIgnoreCase(nomSerie)) {
                        JSONArray editions = ref.optJSONArray("editions");
                        if (editions != null && editions.length() > 0) {
                            JSONObject editObj = editions.getJSONObject(0);
                            String rawNb = editObj.optString("nb_tomes", "0");
                            totalTheorique = Integer.parseInt(rawNb.replaceAll("[^0-9]", ""));
                            editeurRef = editObj.optString("editeur", "");
                        }
                        break;
                    }
                }

                // Étape B : On vérifie l'état de la série pour l'utilisateur
                JSONArray mangas = serie.optJSONArray("mangas");
                int countSouhaitsInSerie = 0;
                boolean possedeAuMoinsUn = false;

                if (mangas != null) {
                    for (int k = 0; k < mangas.length(); k++) {
                        JSONObject m = mangas.getJSONObject(k);
                        // On compte les tomes marqués "souhaiter"
                        if (m.optBoolean("souhaiter", false)) countSouhaitsInSerie++;
                        // On vérifie si l'utilisateur possède déjà un tome de cette série
                        if (m.optBoolean("posséder", false)) possedeAuMoinsUn = true;
                    }
                }

                // Étape C : CONDITION D'AFFICHAGE
                // La série apparaît si elle contient des souhaits MAIS aucun tome possédé
                if (countSouhaitsInSerie > 0 && !possedeAuMoinsUn) {
                    JSONObject serieSouhait = new JSONObject(serie.toString());

                    // On injecte les données calculées pour le SerieAdapter
                    serieSouhait.put("affichage_souhaiter", true); // Flag pour le mode visuel "Wishlist"
                    serieSouhait.put("nombre_tome_total", totalTheorique);
                    serieSouhait.put("editeur_fr", editeurRef);

                    filteredList.add(serieSouhait);
                    nbTomesSouhaiter += countSouhaitsInSerie;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 4. MISE À JOUR DYNAMIQUE DU TITRE (Gestion du singulier/pluriel)
        if (filteredList.size() > 1) {
            tvSouhaits.setText(filteredList.size() + " Séries Souhaitées");
        } else {
            tvSouhaits.setText(filteredList.size() + " Série Souhaitée");
        }

        // 5. Configuration du RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }
}