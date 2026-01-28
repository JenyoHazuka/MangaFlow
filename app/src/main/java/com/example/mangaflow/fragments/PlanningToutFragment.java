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

/**
 * Fragment affichant le planning complet de toutes les sorties à venir.
 * Les données sont groupées par date pour une meilleure lisibilité.
 */
public class PlanningToutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. Liaison avec le layout générique du planning
        View view = inflater.inflate(R.layout.fragment_planning_generic, container, false);
        RecyclerView rv = view.findViewById(R.id.rv_planning_list);

        // Liste hétérogène : contiendra des String (titres de date) et des JSONObject (détails du manga)
        List<Object> items = new ArrayList<>();
        String lastDate = "";

        try {
            // Accès aux méthodes de l'activité parente (PlanningActivity)
            PlanningActivity activity = (PlanningActivity) requireActivity();
            JSONArray data = activity.getMangasData();

            // 2. FILTRAGE : On ne garde que ce qui sort aujourd'hui ou plus tard
            List<JSONObject> filteredList = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject m = data.getJSONObject(i);
                String dateStr = m.optString("date_parution", "");

                // Utilisation de la méthode utilitaire de PlanningActivity
                if (activity.isFutureOrToday(dateStr)) {
                    filteredList.add(m);
                }
            }

            // 3. TRI CHRONOLOGIQUE : On compare les dates pour mettre les sorties les plus proches en haut
            Collections.sort(filteredList, (a, b) -> {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                    return sdf.parse(a.optString("date_parution")).compareTo(sdf.parse(b.optString("date_parution")));
                } catch (Exception e) {
                    return 0;
                }
            });

            // 4. CONSTRUCTION DE LA LISTE AVEC EN-TÊTES
            // On parcourt la liste triée pour insérer une String de date dès que la date change
            for (JSONObject m : filteredList) {
                String date = m.optString("date_parution");

                // Si la date du manga actuel est différente de la précédente, on crée une section
                if (!date.equals(lastDate)) {
                    items.add(date); // Ajout du séparateur visuel (String)
                    lastDate = date;
                }
                items.add(m); // Ajout de l'objet manga (JSONObject)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5. AFFICHAGE : Attribution du gestionnaire de mise en page et de l'Adapter
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new PlanningAdapter(items));

        return view;
    }
}