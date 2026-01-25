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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CompleterFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completer, container, false);

        CollectionActivity activity = (CollectionActivity) getActivity();
        JSONArray collection = (activity != null) ? activity.getCollection() : new JSONArray();

        TextView tvManquants = view.findViewById(R.id.tv_manquants_tomes);
        int totalManquants = 0;

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                int nbTotalSerie = serie.getInt("nombre_tome_total");

                JSONArray mangas = serie.getJSONArray("mangas");
                int possedes = 0;
                for (int j = 0; j < mangas.length(); j++) {
                    if (mangas.getJSONObject(j).getBoolean("posséder")) {
                        possedes++;
                    }
                }
                // Calcul de la différence
                totalManquants += (nbTotalSerie - possedes);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tvManquants.setText(totalManquants + " Tomes manquants");
        return view;
    }
}