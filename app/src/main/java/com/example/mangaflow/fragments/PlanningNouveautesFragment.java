package com.example.mangaflow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.activities.PlanningActivity;
import com.example.mangaflow.utils.PlanningAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlanningNouveautesFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planning_generic, container, false);
        RecyclerView rv = view.findViewById(R.id.rv_planning_list);

        List<Object> items = new ArrayList<>();
        String lastDate = "";

        try {
            PlanningActivity activity = (PlanningActivity) requireActivity();
            JSONArray data = activity.getMangasData();

            // 1. Filtrer (Date + Tome 1)
            List<JSONObject> filteredList = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject m = data.getJSONObject(i);
                String dateStr = m.optString("date_parution", "");
                String numero = m.optString("numero_tome", "");

                if (activity.isFutureOrToday(dateStr) && numero.equals("1")) {
                    filteredList.add(m);
                }
            }

            // 2. TRIER PAR DATE
            Collections.sort(filteredList, (a, b) -> {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                    return sdf.parse(a.optString("date_parution")).compareTo(sdf.parse(b.optString("date_parution")));
                } catch (Exception e) { return 0; }
            });

            // 3. Ajouter les items
            for (JSONObject m : filteredList) {
                String date = m.optString("date_parution");
                if (!date.equals(lastDate)) {
                    items.add(date);
                    lastDate = date;
                }
                items.add(m);
            }
        } catch (Exception e) { e.printStackTrace(); }

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new PlanningAdapter(items));
        return view;
    }
}