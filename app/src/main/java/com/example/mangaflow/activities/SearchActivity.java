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

public class SearchActivity extends BaseActivity {

    private SearchAdapter adapter;
    private Button btnAuteurs, btnEditeurs, btnSeries;
    private String currentCategory = "Auteurs";

    private List<JSONObject> listAuteurs = new ArrayList<>();
    private List<JSONObject> listEditeurs = new ArrayList<>();
    private List<JSONObject> listSeries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        loadJsonData();

        btnAuteurs = findViewById(R.id.btn_auteurs);
        btnEditeurs = findViewById(R.id.btn_editeurs);
        btnSeries = findViewById(R.id.btn_series);
        EditText searchBar = findViewById(R.id.search_bar);
        RecyclerView rv = findViewById(R.id.recyclerView);

        // CONFIGURATION DU CLICK AVEC REDIRECTION VERS SERIEACTIVITY
        adapter = new SearchAdapter(listAuteurs, "nom", data -> {
            Intent intent;
            if (currentCategory.equals("Auteurs")) {
                intent = new Intent(this, AuteurActivity.class);
            } else if (currentCategory.equals("Editeurs")) {
                intent = new Intent(this, EditorActivity.class);
            } else {
                // REDIRECTION VERS LA PAGE SÉRIE
                intent = new Intent(this, SerieActivity.class);
            }
            intent.putExtra("DATA_JSON", data.toString());
            startActivity(intent);
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable editable) {}
        });

        btnAuteurs.setOnClickListener(v -> { currentCategory = "Auteurs"; updateCategory(listAuteurs, "nom", btnAuteurs); });
        btnEditeurs.setOnClickListener(v -> { currentCategory = "Editeurs"; updateCategory(listEditeurs, "nom", btnEditeurs); });
        btnSeries.setOnClickListener(v -> { currentCategory = "Series"; updateCategory(listSeries, "titre", btnSeries); });

        setupNavigation();
    }

    private void loadJsonData() {
        listAuteurs = parseRawJson("auteurs", "nom"); //
        listSeries = parseRawJson("series", "titre");  //
        listEditeurs = parseRawJson("editeurs", "nom"); //
    }

    private List<JSONObject> parseRawJson(String resName, String sortField) {
        List<JSONObject> results = new ArrayList<>();
        try {
            int resId = getResources().getIdentifier(resName, "raw", getPackageName());
            InputStream is = getResources().openRawResource(resId);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONArray array = new JSONArray(new String(buffer, StandardCharsets.UTF_8));
            for (int i = 0; i < array.length(); i++) { results.add(array.getJSONObject(i)); }

            // TRI ALPHABÉTIQUE
            Collections.sort(results, (a, b) -> {
                try { return a.getString(sortField).compareToIgnoreCase(b.getString(sortField)); }
                catch (Exception e) { return 0; }
            });
        } catch (Exception e) { Log.e("JSON_ERROR", "Err: " + resName, e); }
        return results;
    }

    private void updateCategory(List<JSONObject> data, String field, Button activeBtn) {
        adapter.updateData(data, field);
        Button[] buttons = {btnAuteurs, btnEditeurs, btnSeries};
        for (Button b : buttons) {
            b.setBackgroundResource(R.drawable.btn_category_inactive);
            b.setTextColor(Color.BLACK); // TEXTE NOIR POUR INACTIF
        }
        activeBtn.setBackgroundResource(R.drawable.btn_category_active);
        activeBtn.setTextColor(Color.WHITE);
    }

    private void setupNavigation() {
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
    }
}