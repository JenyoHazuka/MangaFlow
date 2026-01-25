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
import org.json.JSONObject;

public class ALireFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_a_lire, container, false);

        // Récupération des données depuis l'activité
        CollectionActivity activity = (CollectionActivity) getActivity();
        JSONArray collection = (activity != null) ? activity.getCollection() : new JSONArray();

        TextView tvALire = view.findViewById(R.id.tv_alire_title);
        int aLireCount = 0;

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONArray mangas = collection.getJSONObject(i).getJSONArray("mangas");
                for (int j = 0; j < mangas.length(); j++) {
                    JSONObject m = mangas.getJSONObject(j);
                    // Logique : Possédé ET Non Lu
                    if (m.getBoolean("posséder") && !m.getBoolean("lu")) {
                        aLireCount++;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tvALire.setText(aLireCount + " Tomes à lire");
        return view;
    }
}