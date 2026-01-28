package com.example.mangaflow.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.utils.SearchAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Activité permettant de rechercher des auteurs, des éditeurs ou des séries.
 * Elle utilise un système d'onglets pour basculer entre les catégories.
 */
public class SearchActivity extends BaseActivity {

    private SearchAdapter adapter;
    private Button btnAuteurs, btnEditeurs, btnSeries;
    private String currentCategory = "Auteurs"; // Catégorie active par défaut

    // Listes pour stocker les objets JSON chargés depuis les ressources raw
    private List<JSONObject> listAuteurs = new ArrayList<>();
    private List<JSONObject> listEditeurs = new ArrayList<>();
    private List<JSONObject> listSeries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 1. Chargement des données JSON au démarrage
        loadJsonData();

        // 2. Initialisation des vues UI
        btnAuteurs = findViewById(R.id.btn_auteurs);
        btnEditeurs = findViewById(R.id.btn_editeurs);
        btnSeries = findViewById(R.id.btn_series);
        EditText searchBar = findViewById(R.id.search_bar);
        RecyclerView rv = findViewById(R.id.recyclerView);

        // 3. CONFIGURATION DE L'ADAPTER ET DU CLIC
        // On définit ici le comportement lors du clic sur un résultat de recherche
        adapter = new SearchAdapter(listAuteurs, "nom", data -> {
            Intent intent;
            if (currentCategory.equals("Auteurs")) {
                intent = new Intent(this, AuteurActivity.class);

                // Récupération et nettoyage du nom (ex: "Nom (Rôle)" -> "Nom")
                String nomComplet = data.optString("nom");
                String nomNettoye = nomComplet.split("\\(")[0].trim();

                intent.putExtra("nom_auteur", nomNettoye);

            } else if (currentCategory.equals("Editeurs")) {
                intent = new Intent(this, EditorActivity.class);
                // On passe l'objet JSON complet sous forme de String
                intent.putExtra("DATA_JSON", data.toString());

            } else {
                // Pour les séries, on redirige vers SerieActivity
                intent = new Intent(this, SerieActivity.class);
                intent.putExtra("SERIE_NAME", data.optString("titre"));
            }
            startActivity(intent);
        });

        // Liaison de l'adapter au RecyclerView (format liste verticale)
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 4. GESTION DU FILTRAGE EN TEMPS RÉEL (Barre de recherche)
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                // On demande à l'adapter de filtrer la liste selon la saisie
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable editable) {}
        });

        // 5. LISTENERS DES BOUTONS DE CATÉGORIE
        btnAuteurs.setOnClickListener(v -> { currentCategory = "Auteurs"; updateCategory(listAuteurs, "nom", btnAuteurs); });
        btnEditeurs.setOnClickListener(v -> { currentCategory = "Editeurs"; updateCategory(listEditeurs, "nom", btnEditeurs); });
        btnSeries.setOnClickListener(v -> { currentCategory = "Series"; updateCategory(listSeries, "titre", btnSeries); });

        // Configuration du menu de navigation inférieur
        setupNavigation();
    }

    /**
     * Appelle le parsing pour chaque fichier JSON nécessaire à la recherche.
     */
    private void loadJsonData() {
        listAuteurs = parseRawJson("auteurs", "nom");
        listSeries = parseRawJson("series", "titre");
        listEditeurs = parseRawJson("editeurs", "nom");
    }

    /**
     * Lit un fichier JSON brut, nettoie les données et les trie par ordre alphabétique.
     * @param resName Nom du fichier dans /raw/
     * @param sortField Clé JSON utilisée pour le tri (nom ou titre)
     * @return Une liste d'objets JSON triée.
     */
    private List<JSONObject> parseRawJson(String resName, String sortField) {
        List<JSONObject> results = new ArrayList<>();
        try {
            // Récupération dynamique de l'ID de la ressource
            int resId = getResources().getIdentifier(resName, "raw", getPackageName());
            InputStream is = getResources().openRawResource(resId);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONArray array = new JSONArray(new String(buffer, StandardCharsets.UTF_8));

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                // LOGIQUE DE NETTOYAGE : Supprimer les rôles entre parenthèses pour les auteurs
                if (resName.equals("auteurs") && obj.has("nom")) {
                    String nomBrut = obj.getString("nom");
                    if (nomBrut.contains("(")) {
                        String nomNettoye = nomBrut.split("\\(")[0].trim();
                        obj.put("nom", nomNettoye);
                    }
                }
                results.add(obj);
            }

            // TRI ALPHABÉTIQUE : Comparaison des chaînes de caractères (insensible à la casse)
            Collections.sort(results, (a, b) -> {
                try { return a.getString(sortField).compareToIgnoreCase(b.getString(sortField)); }
                catch (Exception e) { return 0; }
            });
        } catch (Exception e) { Log.e("JSON_ERROR", "Err lors du parsing : " + resName, e); }
        return results;
    }

    /**
     * Met à jour l'affichage de l'onglet sélectionné (visuels et données de l'adapter).
     */
    private void updateCategory(List<JSONObject> data, String field, Button activeBtn) {
        adapter.updateData(data, field);

        // Mise à jour de l'apparence des boutons (Actif vs Inactif)
        Button[] buttons = {btnAuteurs, btnEditeurs, btnSeries};
        for (Button b : buttons) {
            b.setBackgroundResource(R.drawable.btn_category_inactive);
            b.setTextColor(Color.BLACK);
        }
        activeBtn.setBackgroundResource(R.drawable.btn_category_active);
        activeBtn.setTextColor(Color.WHITE);
    }

    /**
     * Configuration des liens vers les autres activités principales.
     */
    private void setupNavigation() {
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_planning).setOnClickListener(v -> startActivity(new Intent(this, PlanningActivity.class)));
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
    }
}