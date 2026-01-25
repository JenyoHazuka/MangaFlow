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

public class SouhaiterFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_souhaiter, container, false);

        // Récupération des données depuis l'activité
        CollectionActivity activity = (CollectionActivity) getActivity();
        JSONArray collection = (activity != null) ? activity.getCollection() : new JSONArray();

        TextView tvSouhaits = view.findViewById(R.id.tv_souhaiter_title);
        int nbSouhaits = 0;

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONArray mangas = collection.getJSONObject(i).getJSONArray("mangas");
                for (int j = 0; j < mangas.length(); j++) {
                    // Vérifie ton booléen "souhaiter"
                    if (mangas.getJSONObject(j).getBoolean("souhaiter")) {
                        nbSouhaits++;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tvSouhaits.setText(nbSouhaits + " Tomes souhaités");
        return view;
    }
}