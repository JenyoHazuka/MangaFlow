package com.example.mangaflow.fragments;

import android.os.Bundle;
import android.util.Log;
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

public class CollectionFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        CollectionActivity activity = (CollectionActivity) getActivity();
        if (activity == null) return view;

        JSONArray collection = activity.getCollection();
        JSONArray seriesRef = activity.getSeriesReference(); // Lit res/raw/series.json

        TextView tvTomes = view.findViewById(R.id.tv_total_tomes);
        TextView tvSeries = view.findViewById(R.id.tv_total_series);
        RecyclerView rv = view.findViewById(R.id.rv_collection);

        int countTotalTomes = 0;
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                String nomSerie = serie.optString("nom");

                // 1. Jointure avec series.JSON pour Total et Statut
                int totalTheorique = 0;
                String statut = "";
                for (int j = 0; j < seriesRef.length(); j++) {
                    JSONObject ref = seriesRef.getJSONObject(j);
                    if (ref.optString("titre").equalsIgnoreCase(nomSerie)) {
                        JSONArray editions = ref.optJSONArray("editions");
                        if (editions != null && editions.length() > 0) {
                            JSONObject firstEd = editions.getJSONObject(0);
                            String rawNb = firstEd.optString("nb_tomes", "0");
                            totalTheorique = Integer.parseInt(rawNb.replaceAll("[^0-9]", ""));
                            statut = firstEd.optString("statut", "");
                        }
                        break;
                    }
                }

                // 2. Calcul des possédés
                JSONArray mangas = serie.optJSONArray("mangas");
                int nbPossedes = 0;
                if (mangas != null) {
                    for (int k = 0; k < mangas.length(); k++) {
                        if (mangas.getJSONObject(k).optBoolean("posséder", false)) {
                            nbPossedes++;
                            countTotalTomes++;
                        }
                    }
                }

                // 3. Préparation pour l'Adapter (Mode Collection)
                if (nbPossedes > 0) {
                    serie.put("nb_possedes", nbPossedes);
                    serie.put("nombre_tome_total", totalTheorique);
                    serie.put("statut", statut);
                    serie.put("affichage_collection", true); // Active la barre de progression
                    filteredList.add(serie);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        tvTomes.setText(countTotalTomes + " Tomes");
        tvSeries.setText(filteredList.size() + " Séries");

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }
}