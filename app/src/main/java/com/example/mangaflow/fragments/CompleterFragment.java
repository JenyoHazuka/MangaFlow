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
 * Fragment dédié aux séries incomplètes.
 * Il calcule la différence entre les tomes possédés et le nombre total de tomes parus.
 */
public class CompleterFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. Initialisation de la vue
        View view = inflater.inflate(R.layout.fragment_completer, container, false);

        // 2. Récupération des données depuis l'activité parente (CollectionActivity)
        CollectionActivity activity = (CollectionActivity) getActivity();
        if (activity == null) return view;

        JSONArray collection = activity.getCollection(); // Données de l'utilisateur
        JSONArray seriesRef = activity.getSeriesReference(); // Données globales (raw/series.json)

        // Liaison des éléments UI
        RecyclerView rv = view.findViewById(R.id.rv_completer);
        TextView tvManquantsTomes = view.findViewById(R.id.tv_manquants_tomes);
        TextView tvSubtitle = view.findViewById(R.id.tv_completer_subtitle);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        List<JSONObject> filteredList = new ArrayList<>();
        int totalMissingTomes = 0; // Compteur global pour le header

        try {
            // 3. LOGIQUE DE COMPARAISON (Jointure entre les deux JSON)
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                String nomSerie = serie.optString("nom");

                int totalTheorique = 0;
                String editeurRef = "";

                // Recherche du nombre total de tomes dans le référentiel global (series.json)
                for (int j = 0; j < seriesRef.length(); j++) {
                    JSONObject ref = seriesRef.getJSONObject(j);
                    if (ref.optString("titre").equalsIgnoreCase(nomSerie)) {
                        JSONArray editions = ref.optJSONArray("editions");
                        if (editions != null && editions.length() > 0) {
                            JSONObject editObj = editions.getJSONObject(0);
                            String rawNb = editObj.optString("nb_tomes", "0");
                            // Transformation de "34 tomes" en entier 34
                            totalTheorique = Integer.parseInt(rawNb.replaceAll("[^0-9]", ""));
                            editeurRef = editObj.optString("editeur", "");
                        }
                        break;
                    }
                }

                // Comptage des tomes réellement possédés par l'utilisateur
                JSONArray mangas = serie.optJSONArray("mangas");
                int nbPossedes = 0;
                if (mangas != null) {
                    for (int k = 0; k < mangas.length(); k++) {
                        if (mangas.getJSONObject(k).optBoolean("posséder", false)) nbPossedes++;
                    }
                }

                // 4. FILTRAGE : On n'affiche la série que si elle est entamée mais non terminée
                if (nbPossedes > 0 && nbPossedes < totalTheorique) {
                    // On crée une copie pour ne pas modifier l'objet original en mémoire
                    JSONObject serieACompleter = new JSONObject(serie.toString());

                    // Ajout des données calculées pour l'affichage dans l'Adapter
                    serieACompleter.put("nombre_tome_total", totalTheorique);
                    serieACompleter.put("nb_possedes", nbPossedes);
                    serieACompleter.put("nb_manquant", (totalTheorique - nbPossedes));
                    serieACompleter.put("editeur_fr", editeurRef);

                    // Flag pour que le SerieAdapter sache qu'il doit afficher le texte "X tomes manquants"
                    serieACompleter.put("is_completer_view", true);

                    filteredList.add(serieACompleter);
                    totalMissingTomes += (totalTheorique - nbPossedes);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 5. Mise à jour des textes de résumé en haut de page
        tvManquantsTomes.setText(totalMissingTomes + " Tomes manquants");
        tvSubtitle.setText(filteredList.size() + " Séries à compléter");

        // Rafraîchissement de la liste
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }
}