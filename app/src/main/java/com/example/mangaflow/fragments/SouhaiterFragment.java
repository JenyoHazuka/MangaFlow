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

public class SouhaiterFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_souhaiter, container, false);

        CollectionActivity activity = (CollectionActivity) getActivity();
        if (activity == null) return view;

        JSONArray collection = activity.getCollection();
        JSONArray seriesRef = activity.getSeriesReference();

        TextView tvSouhaits = view.findViewById(R.id.tv_souhaiter_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_souhaiter_subtitle);
        RecyclerView rv = view.findViewById(R.id.rv_souhaiter);

        int nbTomesSouhaiter = 0;
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                String nomSerie = serie.optString("nom");

                // 1. Jointure avec series.JSON
                int totalTheorique = 0;
                for (int j = 0; j < seriesRef.length(); j++) {
                    JSONObject ref = seriesRef.getJSONObject(j);
                    if (ref.optString("titre").equalsIgnoreCase(nomSerie)) {
                        JSONArray editions = ref.optJSONArray("editions");
                        if (editions != null && editions.length() > 0) {
                            String rawNb = editions.getJSONObject(0).optString("nb_tomes", "0");
                            totalTheorique = Integer.parseInt(rawNb.replaceAll("[^0-9]", ""));
                        }
                        break;
                    }
                }

                // 2. Vérification des critères
                JSONArray mangas = serie.optJSONArray("mangas");
                boolean aEnvie = false;
                boolean possedeAuMoinsUn = false;
                int countSouhaitsSerie = 0;

                if (mangas != null) {
                    for (int k = 0; k < mangas.length(); k++) {
                        JSONObject m = mangas.getJSONObject(k);
                        if (m.optBoolean("souhaiter", false)) {
                            aEnvie = true;
                            countSouhaitsSerie++;
                        }
                        if (m.optBoolean("posséder", false)) possedeAuMoinsUn = true;
                    }
                }

                // 3. Affichage si souhaité et 0 possédé
                if (aEnvie && !possedeAuMoinsUn) {
                    serie.remove("affichage_collection"); // Désactive le mode barre de progression
                    serie.put("affichage_souhaiter", true);
                    serie.put("nombre_tome_total", totalTheorique);
                    filteredList.add(serie);
                    nbTomesSouhaiter += countSouhaitsSerie;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        tvSouhaits.setText(nbTomesSouhaiter + " Tomes souhaités");
        tvSubtitle.setText(filteredList.size() + " Séries");

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }
}