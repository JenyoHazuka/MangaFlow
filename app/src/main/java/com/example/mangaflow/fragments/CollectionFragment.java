package com.example.mangaflow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.example.mangaflow.R;
import com.example.mangaflow.activities.CollectionActivity;

import org.json.JSONArray;
import org.json.JSONException;

public class CollectionFragment extends Fragment {
    // Dans CollectionFragment.java
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        // Récupération des données depuis l'activité
        CollectionActivity activity = (CollectionActivity) getActivity();
        JSONArray collection = (activity != null) ? activity.getCollection() : new JSONArray();

        // Initialisation des TextViews
        TextView tvTomes = view.findViewById(R.id.tv_total_tomes);
        TextView tvSeries = view.findViewById(R.id.tv_total_series);

        int countTomes = 0;
        int countSeries = collection.length(); // Nombre d'objets dans le tableau principal

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONArray mangas = collection.getJSONObject(i).getJSONArray("mangas");
                for (int j = 0; j < mangas.length(); j++) {
                    // On vérifie le booléen "posséder"
                    if (mangas.getJSONObject(j).getBoolean("posséder")) {
                        countTomes++;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Mise à jour de l'UI
        tvTomes.setText(countTomes + " Tomes");
        tvSeries.setText(countSeries + " Séries suivies");

        return view;
    }
}