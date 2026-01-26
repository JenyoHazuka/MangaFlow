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

public class CompleterFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completer, container, false);

        CollectionActivity activity = (CollectionActivity) getActivity();
        JSONArray collection = (activity != null) ? activity.getCollection() : new JSONArray();

        TextView tvManquants = view.findViewById(R.id.tv_manquants_tomes);
        RecyclerView rv = view.findViewById(R.id.rv_completer);

        int totalManquants = 0;
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                int total = serie.optInt("nombre_tome_total", 0);

                JSONArray mangas = serie.getJSONArray("mangas");
                int possedes = 0;
                for (int j = 0; j < mangas.length(); j++) {
                    if (mangas.getJSONObject(j).getBoolean("posséder")) possedes++;
                }

                // On affiche si on a commencé la série (possedes > 0) mais qu'elle est incomplète
                if (possedes > 0 && possedes < total) {
                    filteredList.add(serie);
                    totalManquants += (total - possedes);
                }
            }
        } catch (JSONException e) { e.printStackTrace(); }

        tvManquants.setText(totalManquants + " Tomes manquants");
        ((TextView) view.findViewById(R.id.tv_completer_subtitle)).setText(filteredList.size() + " Séries à compléter");
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }
}