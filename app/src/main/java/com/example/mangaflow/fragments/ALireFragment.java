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

/**
 * Fragment affichant la liste des mangas possédés mais non lus ("À lire").
 */
public class ALireFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. INFLATION : Chargement du layout XML du fragment
        View view = inflater.inflate(R.layout.fragment_a_lire, container, false);

        // 2. RÉCUPÉRATION DES DONNÉES : On accède à la collection chargée par l'activité parente
        CollectionActivity activity = (CollectionActivity) getActivity();
        JSONArray collection = (activity != null) ? activity.getCollection() : new JSONArray();

        // Initialisation des vues
        TextView tvALire = view.findViewById(R.id.tv_alire_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_alire_subtitle);
        RecyclerView rv = view.findViewById(R.id.rv_alire);

        // Configuration du RecyclerView (Liste verticale standard)
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        int totalALire = 0; // Compteur global des tomes à lire
        List<JSONObject> filteredList = new ArrayList<>(); // Liste des séries contenant des tomes à lire

        try {
            // 3. LOGIQUE DE FILTRAGE
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);

                /* NETTOYAGE : On retire les drapeaux (flags) utilisés par les autres fragments
                   pour éviter les conflits d'affichage dans le SerieAdapter.
                */
                serie.remove("affichage_collection");
                serie.remove("nb_manquant");
                serie.remove("affichage_souhaiter");

                JSONArray mangas = serie.optJSONArray("mangas");
                int countALireSerie = 0; // Compteur spécifique à cette série

                if (mangas != null) {
                    for (int j = 0; j < mangas.length(); j++) {
                        JSONObject m = mangas.getJSONObject(j);

                        /* CONDITION CLÉ : Le manga doit être marqué comme "possédé"
                           ET le marqueur "lu" doit être à false.
                        */
                        if (m.optBoolean("posséder", false) && !m.optBoolean("lu", false)) {
                            countALireSerie++;
                        }
                    }
                }

                // Si la série contient au moins un tome à lire, on l'ajoute à l'affichage
                if (countALireSerie > 0) {
                    // On injecte le nombre calculé dans l'objet JSON pour que l'adapter puisse l'afficher
                    serie.put("nb_a_lire", countALireSerie);
                    filteredList.add(serie);
                    totalALire += countALireSerie;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4. MISE À JOUR DE L'INTERFACE
        tvALire.setText(totalALire + " Tomes à lire");
        tvSubtitle.setText(filteredList.size() + " Séries");

        // Liaison des données filtrées à l'adapter
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }
}