package com.example.mangaflow.fragments;

import android.os.Bundle;
import android.util.Log;
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

public class ALireFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_a_lire, container, false);

        CollectionActivity activity = (CollectionActivity) getActivity();
        JSONArray collection = (activity != null) ? activity.getCollection() : new JSONArray();

        TextView tvALire = view.findViewById(R.id.tv_alire_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_alire_subtitle);
        RecyclerView rv = view.findViewById(R.id.rv_alire);

        // On utilise getContext() ici pour dire au RecyclerView comment s'afficher
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        int aLireCount = 0;
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                JSONArray mangas = serie.optJSONArray("mangas");

                if (mangas == null) continue;

                boolean serieHasUnread = false;
                for (int j = 0; j < mangas.length(); j++) {
                    JSONObject m = mangas.getJSONObject(j);

                    // On vérifie si possédé ET non lu
                    if (m.optBoolean("posséder", false) && !m.optBoolean("lu", false)) {
                        aLireCount++;
                        serieHasUnread = true;
                    }
                }

                if (serieHasUnread) {
                    filteredList.add(serie);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- AFFICHAGE ---
        if (filteredList.isEmpty()) {
            tvALire.setText("Tout est lu !");
            tvSubtitle.setText("0 Séries");
        } else {
            tvALire.setText(aLireCount + " Tomes à lire");
            tvSubtitle.setText(filteredList.size() + " Séries");

            // UTILISATION DE TON CONSTRUCTEUR ACTUEL :
            SerieAdapter adapter = new SerieAdapter(filteredList);
            rv.setAdapter(adapter);
        }

        return view;
    }
}