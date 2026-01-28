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
 * Fragment affichant uniquement les lancements de nouvelles séries (Tome 1).
 * Organise les sorties par ordre chronologique avec des en-têtes de date.
 */
public class PlanningNouveautesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. Initialisation de la vue générique du planning
        View view = inflater.inflate(R.layout.fragment_planning_generic, container, false);
        RecyclerView rv = view.findViewById(R.id.rv_planning_list);

        // Cette liste contiendra à la fois des String (dates) et des JSONObject (mangas)
        List<Object> items = new ArrayList<>();
        String lastDate = "";

        try {
            // Récupération de l'activité parente pour accéder aux données et à l'utilitaire de date
            PlanningActivity activity = (PlanningActivity) requireActivity();
            JSONArray data = activity.getMangasData();

            // 2. ÉTAPE DE FILTRAGE : On ne garde que les futurs Tomes 1
            List<JSONObject> filteredList = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject m = data.getJSONObject(i);
                String dateStr = m.optString("date_parution", "");
                String numero = m.optString("numero_tome", "");

                // Double condition : Date >= aujourd'hui ET numéro de tome == "1"
                if (activity.isFutureOrToday(dateStr) && numero.equals("1")) {
                    filteredList.add(m);
                }
            }

            // 3. ÉTAPE DE TRI : Classement chronologique (du plus proche au plus lointain)
            Collections.sort(filteredList, (a, b) -> {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                    return sdf.parse(a.optString("date_parution")).compareTo(sdf.parse(b.optString("date_parution")));
                } catch (Exception e) { return 0; }
            });

            // 4. ÉTAPE DE CONSTRUCTION : Insertion des séparateurs de dates
            for (JSONObject m : filteredList) {
                String date = m.optString("date_parution");

                // Si la date change par rapport au manga précédent, on ajoute un en-tête String
                if (!date.equals(lastDate)) {
                    items.add(date); // L'adapter reconnaîtra ce String comme un titre de section
                    lastDate = date;
                }
                items.add(m); // Ajout du manga sous sa date correspondante
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5. Configuration finale du RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new PlanningAdapter(items));

        return view;
    }
}