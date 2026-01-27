package com.example.mangaflow.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangaflow.R;
import com.example.mangaflow.utils.EditorAdapter;
import com.example.mangaflow.utils.MangaClass;

import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private List<MangaClass> mangaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // 1. Initialisation des données
        mangaList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            // On crée un nouvel objet MangaClass avec le titre souhaité
            MangaClass manga = new MangaClass();
            manga.setTitre("Nom manga " + i); // Utilise le setter de ta classe
            mangaList.add(manga);
        }

        // 2. Configuration du RecyclerView
        recyclerView = findViewById(R.id.recycler_mangas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Liaison avec l'Adapter (le moteur de la liste)
        EditorAdapter adapter = new EditorAdapter(mangaList);
        recyclerView.setAdapter(adapter);

        // Bouton retour
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}