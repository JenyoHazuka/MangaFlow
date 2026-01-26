package com.example.mangaflow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.activities.CollectionActivity;
import com.example.mangaflow.utils.SerieAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SouhaiterFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_souhaiter, container, false);

        CollectionActivity activity = (CollectionActivity) getActivity();
        JSONArray collection = (activity != null) ? activity.getCollection() : new JSONArray();

        TextView tvSouhaits = view.findViewById(R.id.tv_souhaiter_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_souhaiter_subtitle);
        RecyclerView rv = view.findViewById(R.id.rv_souhaiter);

        int nbTomesSouhaiter = 0; // C'est cette variable que tu utilises
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                JSONArray mangas = serie.getJSONArray("mangas");

                boolean aEnvie = false;
                boolean possèdeAuMoinsUn = false;
                int countForSerie = 0;

                for (int j = 0; j < mangas.length(); j++) {
                    JSONObject m = mangas.getJSONObject(j);
                    // On compte les tomes souhaités
                    if (m.getBoolean("souhaiter")) {
                        aEnvie = true;
                        countForSerie++;
                    }
                    // On vérifie si on en possède déjà
                    if (m.getBoolean("posséder")) {
                        possèdeAuMoinsUn = true;
                    }
                }

                // Critère : On affiche la série seulement si on ne possède rien du tout
                if (aEnvie && !possèdeAuMoinsUn) {
                    filteredList.add(serie);
                    nbTomesSouhaiter += countForSerie;
                }
            }
        } catch (JSONException e) { e.printStackTrace(); }

        // --- CORRECTION DES TEXTES ---
        tvSouhaits.setText(nbTomesSouhaiter + " Tomes souhaités");
        tvSubtitle.setText(filteredList.size() + " Séries");

        // --- CONFIGURATION DU RECYCLERVIEW ---
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }
}