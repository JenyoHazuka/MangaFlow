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

public class CompleterFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completer, container, false);

        CollectionActivity activity = (CollectionActivity) getActivity();
        if (activity == null) return view;

        JSONArray collection = activity.getCollection();
        JSONArray seriesRef = activity.getSeriesReference();

        RecyclerView rv = view.findViewById(R.id.rv_completer);
        TextView tvManquantsTomes = view.findViewById(R.id.tv_manquants_tomes);
        TextView tvSubtitle = view.findViewById(R.id.tv_completer_subtitle);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        List<JSONObject> filteredList = new ArrayList<>();
        int totalMissingTomes = 0;

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                String nomSerie = serie.optString("nom");

                int totalTheorique = 0;
                String editeurRef = "";

                // Recherche dans le référentiel global
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

                // Comptage des possédés
                JSONArray mangas = serie.optJSONArray("mangas");
                int nbPossedes = 0;
                if (mangas != null) {
                    for (int k = 0; k < mangas.length(); k++) {
                        if (mangas.getJSONObject(k).optBoolean("posséder", false)) nbPossedes++;
                    }
                }

                // Condition : possède au moins 1 tome mais pas la totalité
                if (nbPossedes > 0 && nbPossedes < totalTheorique) {
                    JSONObject serieACompleter = new JSONObject(serie.toString());
                    serieACompleter.put("nombre_tome_total", totalTheorique);
                    serieACompleter.put("nb_possedes", nbPossedes);
                    serieACompleter.put("nb_manquant", (totalTheorique - nbPossedes));
                    serieACompleter.put("editeur_fr", editeurRef); // Pour la redirection Intent

                    // On garde un flag pour l'affichage spécifique
                    serieACompleter.put("is_completer_view", true);

                    filteredList.add(serieACompleter);
                    totalMissingTomes += (totalTheorique - nbPossedes);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        tvManquantsTomes.setText(totalMissingTomes + " Tomes manquants");
        tvSubtitle.setText(filteredList.size() + " Séries à compléter");
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }
}