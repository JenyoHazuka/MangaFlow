package com.example.mangaflow.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangaflow.utils.MangaAdapter;
import com.example.mangaflow.utils.MangaClass;
import com.example.mangaflow.R;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MangaAdapter adapter;
    private List<MangaClass> mangaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 1. Charger la liste depuis le JSON
        if (isNetworkAvailable()) {
            mangaList = loadMangasFromJSON();

            // 2. Créer l'adapter AVEC la liste remplie
            adapter = new MangaAdapter(mangaList);

            // 3. Lier l'adapter au RecyclerView
            recyclerView.setAdapter(adapter);

            // 4. Forcer l'affichage
            adapter.notifyDataSetChanged();
        } else {
            // Afficher un message d'erreur si pas de Wi-Fi/4G
            Toast.makeText(this, "Pas de connexion internet", Toast.LENGTH_LONG).show();
        };

        // Liaison des boutons XML vers le code Java
        ImageView btnPlanning = findViewById(R.id.btn_planning);
        ImageView btnCollection = findViewById(R.id.btn_collection);
        ImageView btnSearch = findViewById(R.id.btn_search);
        ImageView btnMaps = findViewById(R.id.btn_maps);

        // Configuration des clics

        // Vers le Planning
        btnPlanning.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PlanningActivity.class);
            startActivity(intent);
        });

        // Vers la Collection
        btnCollection.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CollectionActivity.class);
            startActivity(intent);
        });

        // Vers la Recherche
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Vers la Carte
        btnMaps.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MapsActivity.class);
            startActivity(intent);
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private List<MangaClass> loadMangasFromJSON() {
        List<MangaClass> list = new ArrayList<>();
        try {
            // Lecture depuis le dossier 'raw'
            InputStream is = getResources().openRawResource(R.raw.mangas);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            // Parsing du JSON
            JSONArray jsonArray = new JSONArray(json);
            int numTome = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                // On crée l'objet MangaClass avec les clés exactes de ton JSON
                list.add(new MangaClass(
                        obj.getString("titre_serie"),
                        numTome = Integer.parseInt(obj.getString("numero_tome")),
                        obj.getString("image_url"),
                        obj.getString("edition"),
                        obj.getInt("ean"),
                        obj.getString("editeur_fr"),
                        obj.getString("date_parution"),
                        0.0f, // Prix
                        0,    // Pages
                        null, // Liste auteurs
                        null, // Liste genres
                        obj.getString("resume")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}