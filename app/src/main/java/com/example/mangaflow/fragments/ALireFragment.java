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

public class ALireFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_a_lire, container, false);

        CollectionActivity activity = (CollectionActivity) getActivity();
        JSONArray collection = (activity != null) ? activity.getCollection() : new JSONArray();

        TextView tvALire = view.findViewById(R.id.tv_alire_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_alire_subtitle);
        RecyclerView rv = view.findViewById(R.id.rv_alire);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        int totalALire = 0;
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);

                // NETTOYAGE : On retire les drapeaux des autres fragments
                serie.remove("affichage_collection");
                serie.remove("nb_manquant");
                serie.remove("affichage_souhaiter");

                JSONArray mangas = serie.optJSONArray("mangas");
                int countALireSerie = 0;
                if (mangas != null) {
                    for (int j = 0; j < mangas.length(); j++) {
                        JSONObject m = mangas.getJSONObject(j);
                        if (m.optBoolean("posséder", false) && !m.optBoolean("lu", false)) {
                            countALireSerie++;
                        }
                    }
                }

                if (countALireSerie > 0) {
                    serie.put("nb_a_lire", countALireSerie);
                    filteredList.add(serie);
                    totalALire += countALireSerie; // N'oubliez pas d'incrémenter le total ici
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        tvALire.setText(totalALire + " Tomes à lire");
        tvSubtitle.setText(filteredList.size() + " Séries");

        rv.setAdapter(new SerieAdapter(filteredList));
        return view;
    }
}