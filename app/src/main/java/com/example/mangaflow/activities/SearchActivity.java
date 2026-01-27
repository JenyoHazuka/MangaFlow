package com.example.mangaflow.activities;

import android.content.Intent;
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
import java.util.List;

public class SearchActivity extends BaseActivity {

    private SearchAdapter adapter;
    private Button btnAuteurs, btnEditeurs, btnSeries;

    // Listes qui recevront les données des JSON
    private List<String> listeAuteurs = new ArrayList<>();
    private List<String> listeEditeurs = new ArrayList<>();
    private List<String> listeSeries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 1. Charger les données en PREMIER
        loadJsonData();

        // 2. Initialisation des vues
        btnAuteurs = findViewById(R.id.btn_auteurs);
        btnEditeurs = findViewById(R.id.btn_editeurs);
        btnSeries = findViewById(R.id.btn_series);
        EditText searchBar = findViewById(R.id.search_bar);
        RecyclerView rv = findViewById(R.id.recyclerView);

        // 3. Setup RecyclerView
        // On passe la liste déjà remplie (auteurs par défaut)
        adapter = new SearchAdapter(listeAuteurs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 4. TRÈS IMPORTANT : Forcer l'affichage initial
        adapter.updateData(listeAuteurs);

        // Écouteur de recherche
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable editable) {}
        });

        // Gestion des clics boutons
        btnAuteurs.setOnClickListener(v -> updateCategory(listeAuteurs, btnAuteurs));
        btnEditeurs.setOnClickListener(v -> updateCategory(listeEditeurs, btnEditeurs));
        btnSeries.setOnClickListener(v -> updateCategory(listeSeries, btnSeries));

        // Navigation (Correction de l'Intent Search qui pointait vers EditorActivity)
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_planning).setOnClickListener(v -> startActivity(new Intent(this, PlanningActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> { /* Déjà sur cette page */ });
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
    }

    /**
     * Méthode pour charger les données des 3 fichiers JSON
     */
    private void loadJsonData() {
        listeAuteurs = parseJsonField("auteurs.json", "nom");
        listeSeries = parseJsonField("series.json", "titre");
        listeEditeurs = parseJsonField("editeurs.json", "nom");
    }

    /**
     * Lit un fichier dans Assets et extrait un champ spécifique d'un tableau JSON
     */
    private List<String> parseJsonField(String fileName, String fieldName) {
        List<String> results = new ArrayList<>();
        try {
            // Suppression de l'extension .json car openRawResource utilise l'ID R.raw.nom
            String resourceName = fileName.replace(".json", "");
            int resId = getResources().getIdentifier(resourceName, "raw", getPackageName());

            if (resId == 0) {
                Log.e("JSON_ERROR", "Fichier introuvable dans res/raw : " + fileName);
                return results;
            }

            InputStream is = getResources().openRawResource(resId);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.has(fieldName)) {
                    results.add(obj.getString(fieldName));
                }
            }
        } catch (Exception e) {
            Log.e("JSON_ERROR", "Erreur lors du chargement de " + fileName, e);
        }
        return results;
    }

    private void updateCategory(List<String> data, Button activeBtn) {
        adapter.updateData(data);
        // Réinitialiser le style
        int gray = 0xFFF5F5F5; // ou ton R.drawable.btn_category_inactive
        btnAuteurs.setBackgroundResource(R.drawable.btn_category_inactive);
        btnAuteurs.setTextColor(0xFF000000);
        btnEditeurs.setBackgroundResource(R.drawable.btn_category_inactive);
        btnEditeurs.setTextColor(0xFF000000);
        btnSeries.setBackgroundResource(R.drawable.btn_category_inactive);
        btnSeries.setTextColor(0xFF000000);

        // Activer le bouton cliqué
        activeBtn.setBackgroundResource(R.drawable.btn_category_active);
        activeBtn.setTextColor(0xFFFFFFFF);
    }
}