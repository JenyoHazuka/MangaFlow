package com.example.mangaflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.utils.EditorAdapter;
import com.example.mangaflow.utils.MangaClass;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private List<MangaClass> mangaList = new ArrayList<>();

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

                // Remplissage de la liste depuis le champ "mangas"
                JSONArray mangas = data.getJSONArray("mangas");
                for (int i = 0; i < mangas.length(); i++) {
                    MangaClass m = new MangaClass();
                    m.setTitre(mangas.getString(i));
                    mangaList.add(m);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        TextView tvHeader = findViewById(R.id.editor_name);
        if (tvHeader != null) tvHeader.setText(nomEditeur);

        recyclerView = findViewById(R.id.recycler_mangas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new EditorAdapter(mangaList));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}