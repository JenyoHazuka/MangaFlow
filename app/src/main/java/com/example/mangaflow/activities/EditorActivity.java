package com.example.mangaflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.utils.EditorAdapter;
import com.example.mangaflow.models.EditorItem;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditorActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private List<EditorItem> displayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        String jsonStr = getIntent().getStringExtra("DATA_JSON");
        String nomEditeur = "Éditeur";

        try {
            if (jsonStr != null) {
                JSONObject data = new JSONObject(jsonStr);
                nomEditeur = data.getString("nom");

                // On traite chaque section avec tri alphabétique
                processSection("MANGAS", data.optJSONArray("mangas"));
                processSection("LIGHT NOVELS", data.optJSONArray("light_novels"));
                processSection("ARTBOOKS", data.optJSONArray("artbooks"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        TextView tvHeader = findViewById(R.id.editor_name);
        if (tvHeader != null) tvHeader.setText(nomEditeur);

        recyclerView = findViewById(R.id.recycler_mangas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new EditorAdapter(displayList));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        setupNavigation();
    }

    private void processSection(String sectionTitle, JSONArray array) throws Exception {
        if (array != null && array.length() > 0) {
            List<String> tempList = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                tempList.add(array.getString(i));
            }

            // TRI ALPHABÉTIQUE (A-Z)
            Collections.sort(tempList, String.CASE_INSENSITIVE_ORDER);

            // Ajout de l'entête gris
            displayList.add(new EditorItem(EditorItem.TYPE_HEADER, sectionTitle));

            // Ajout des items triés
            for (String title : tempList) {
                displayList.add(new EditorItem(EditorItem.TYPE_MANGA, title));
            }
        }
    }

    private void setupNavigation() {
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }
}