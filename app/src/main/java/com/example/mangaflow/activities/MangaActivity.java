package com.example.mangaflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mangaflow.R;
import com.example.mangaflow.utils.MangaClass;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MangaActivity extends AppCompatActivity {

    // Variable globale pour stocker le manga chargé
    private MangaClass currentManga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga);

        String targetTitle = getIntent().getStringExtra("TITRE_MANGA");

        if (targetTitle != null) {
            loadMangaFromJSON(targetTitle);
        } else {
            Toast.makeText(this, "Erreur : aucun manga spécifié", Toast.LENGTH_SHORT).show();
            finish();
        }

        // --- NAVIGATION ---
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
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
            startActivity(new Intent(this, SearchActivity.class));
        });

        findViewById(R.id.btn_maps).setOnClickListener(v -> {
            startActivity(new Intent(this, MapsActivity.class));
        });

    }

    private void loadMangaFromJSON(String titleToFind) {
        try {
            InputStream is = getResources().openRawResource(R.raw.mangas);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                if (obj.optString("titre_serie").equalsIgnoreCase(titleToFind)) {

                    List<String> auteurs = new ArrayList<>();
                    JSONArray auteursJ = obj.optJSONArray("auteurs");
                    if (auteursJ != null) {
                        for(int j=0; j<auteursJ.length(); j++) auteurs.add(auteursJ.getString(j));
                    }

                    List<String> genres = new ArrayList<>();
                    JSONArray genresJ = obj.optJSONArray("genres_theme");
                    if (genresJ != null) {
                        for(int j=0; j<genresJ.length(); j++) genres.add(genresJ.getString(j));
                    }

                    // Traitement du prix
                    String prixRaw = obj.optString("prix", "0.0");
                    float prixClean = 0.0f;
                    try {
                        prixClean = Float.parseFloat(prixRaw.replace("€", "").replace(",", ".").trim());
                    } catch (NumberFormatException e) {
                        Log.e("MangaFlow", "Erreur format prix: " + prixRaw);
                    }

                    // On assigne à la variable globale currentManga
                    currentManga = new MangaClass(
                            obj.optString("titre_serie"),
                            obj.optInt("numero_tome"),
                            obj.optString("image_url"),
                            obj.optString("edition"),
                            obj.optString("ean", "0000000000000"),
                            obj.optString("editeur_fr"),
                            obj.optString("date_parution"),
                            prixClean,
                            obj.optInt("nb_pages", 0),
                            auteurs,
                            genres,
                            obj.optString("resume")
                    );

                    updateUI(currentManga);
                    setupSerieLinks(); // Active les clics vers la page Série
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("MangaFlow", "Erreur détaillée : " + e.getMessage());
            Toast.makeText(this, "Erreur lecture JSON", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSerieLinks() {
        if (currentManga == null) return;

        // On active le clic uniquement sur la section Série
        findViewById(R.id.tv_serie_name).setOnClickListener(v -> {
            if (currentManga != null) {
                Intent SerieIntent = new Intent(MangaActivity.this, SerieActivity.class);
                SerieIntent.putExtra("SERIE_NAME", currentManga.getTitre_serie());
                SerieIntent.putExtra("EDITEUR_NAME", currentManga.getEditeur());
                startActivity(SerieIntent);
            }
        });

        // L'éditeur reste statique pour l'instant
    }

    private void updateUI(MangaClass manga) {
        ((TextView) findViewById(R.id.tv_header_title)).setText(manga.getTitre_serie());
        ((TextView) findViewById(R.id.tv_info_title)).setText(manga.getTitre_serie());
        ((TextView) findViewById(R.id.tv_info_tome)).setText("Tome " + manga.getNumero_tome());

        ((TextView) findViewById(R.id.tv_serie_name)).setText(manga.getTitre_serie());
        ((TextView) findViewById(R.id.tv_edition_name)).setText(manga.getEdition() + " - " + manga.getEditeur());

        ((TextView) findViewById(R.id.tv_manga_summary)).setText(manga.getResume());

        // Prix et Barcode corrigés
        ((TextView) findViewById(R.id.tv_price)).setText(String.format("€  %.2f€", manga.getPrix()));
        ((TextView) findViewById(R.id.tv_date)).setText("📅  " + manga.getDate_parution());
        ((TextView) findViewById(R.id.tv_isbn)).setText("Barcode  " + manga.getIsbn());
        ((TextView) findViewById(R.id.tv_nb_pages)).setText("Nombre de pages : " + manga.getNb_pages());

        ImageView cover = findViewById(R.id.iv_manga_cover);
        Glide.with(this)
                .load(manga.getImage_url())
                .placeholder(R.drawable.placeholder_cover)
                .error(R.drawable.placeholder_cover)
                .centerCrop()
                .into(cover);

        findViewById(R.id.btn_read).setOnClickListener(v -> {
            Toast.makeText(this, "Ouverture de " + manga.getTitre_serie(), Toast.LENGTH_SHORT).show();
        });
    }
}