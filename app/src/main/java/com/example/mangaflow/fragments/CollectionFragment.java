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

public class CollectionFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        CollectionActivity activity = (CollectionActivity) getActivity();
        JSONArray collection = (activity != null) ? activity.getCollection() : new JSONArray();

        TextView tvTomes = view.findViewById(R.id.tv_total_tomes);
        TextView tvSeries = view.findViewById(R.id.tv_total_series);
        RecyclerView rv = view.findViewById(R.id.rv_collection);

        int countTomes = 0;
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                JSONArray mangas = serie.getJSONArray("mangas");
                int countInSerie = 0;

                for (int j = 0; j < mangas.length(); j++) {
                    if (mangas.getJSONObject(j).getBoolean("posséder")) {
                        countInSerie++;
                        countTomes++;
                    }
                }

                // On n'affiche que si on possède AU MOINS 1 tome
                if (countInSerie > 0) {
                    filteredList.add(serie);
                }
            }
        } catch (JSONException e) { e.printStackTrace(); }

        tvTomes.setText(countTomes + " Tomes");
        tvSeries.setText(filteredList.size() + " Séries");

        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }
}