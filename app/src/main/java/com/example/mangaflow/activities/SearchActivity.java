package com.example.mangaflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangaflow.R;

import java.util.Arrays;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private SearchAdapter adapter;
    private Button btnAuteurs, btnEditeurs, btnSeries;

    // Données de test
    private List<String> listeAuteurs = Arrays.asList("Victor Hugo", "Victor for", "Émile Zola", "Albert Camus", "Molière");
    private List<String> listeEditeurs = Arrays.asList("Gallimard", "Hachette", "Flammarion", "Pocket");
    private List<String> listeSeries = Arrays.asList("Harry Potter", "Asterix", "One Piece", "Tintin");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialisation des vues
        btnAuteurs = findViewById(R.id.btn_auteurs);
        btnEditeurs = findViewById(R.id.btn_editeurs);
        btnSeries = findViewById(R.id.btn_series);
        EditText searchBar = findViewById(R.id.search_bar);
        RecyclerView rv = findViewById(R.id.recyclerView);

        // Setup RecyclerView
        adapter = new SearchAdapter(listeAuteurs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Écouteur de recherche
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Gestion des clics boutons
        btnAuteurs.setOnClickListener(v -> updateCategory(listeAuteurs, btnAuteurs));
        btnEditeurs.setOnClickListener(v -> updateCategory(listeEditeurs, btnEditeurs));
        btnSeries.setOnClickListener(v -> updateCategory(listeSeries, btnSeries));

        // --- NAVIGATION ---
        findViewById(R.id.btn_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });

        findViewById(R.id.btn_collection).setOnClickListener(v -> {
            startActivity(new Intent(this, CollectionActivity.class));
        });

        findViewById(R.id.btn_planning).setOnClickListener(v -> {
            startActivity(new Intent(this, PlanningActivity.class));
        });

        findViewById(R.id.btn_search).setOnClickListener(v -> {
            startActivity(new Intent(this, EditorActivity.class));
        });

        findViewById(R.id.btn_maps).setOnClickListener(v -> {
            startActivity(new Intent(this, MapsActivity.class));
        });
    }

    private void updateCategory(List<String> data, Button activeBtn) {
        // 1. Mettre à jour les données
        adapter.updateData(data);

        // 2. Réinitialiser le style de tous les boutons
        btnAuteurs.setBackgroundResource(R.drawable.btn_category_inactive);
        btnAuteurs.setTextColor(0xFF000000);
        btnEditeurs.setBackgroundResource(R.drawable.btn_category_inactive);
        btnEditeurs.setTextColor(0xFF000000);
        btnSeries.setBackgroundResource(R.drawable.btn_category_inactive);
        btnSeries.setTextColor(0xFF000000);

        // 3. Activer le bouton cliqué
        activeBtn.setBackgroundResource(R.drawable.btn_category_active);
        activeBtn.setTextColor(0xFFFFFFFF);
    }
}