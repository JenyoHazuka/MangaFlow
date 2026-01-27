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
        RecyclerView rv = view.findViewById(R.id.rv_souhaiter);

        int nbTomesSouhaiter = 0;
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                String nomSerie = serie.optString("nom");

                // 1. Jointure pour récupérer le total et l'éditeur
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

                // 2. Vérification : On cherche si des tomes sont marqués "souhaiter"
                JSONArray mangas = serie.optJSONArray("mangas");
                int countSouhaitsInSerie = 0;
                boolean possedeAuMoinsUn = false;

                if (mangas != null) {
                    for (int k = 0; k < mangas.length(); k++) {
                        JSONObject m = mangas.getJSONObject(k);
                        if (m.optBoolean("souhaiter", false)) countSouhaitsInSerie++;
                        if (m.optBoolean("posséder", false)) possedeAuMoinsUn = true;
                    }
                }

                // 3. Logique d'affichage : Séries suivies mais non encore commencées (0 possédés)
                if (countSouhaitsInSerie > 0 && !possedeAuMoinsUn) {
                    JSONObject serieSouhait = new JSONObject(serie.toString());
                    serieSouhait.put("affichage_souhaiter", true);
                    serieSouhait.put("nombre_tome_total", totalTheorique);
                    serieSouhait.put("editeur_fr", editeurRef);

                    filteredList.add(serieSouhait);
                    nbTomesSouhaiter += countSouhaitsInSerie;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // MISE À JOUR DE L'EN-TÊTE
        if (filteredList.size() > 1) {
            tvSouhaits.setText(filteredList.size() + " Séries Souhaitées");
        } else {
            tvSouhaits.setText(filteredList.size() + " Série Souhaitée");
        }

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }
}