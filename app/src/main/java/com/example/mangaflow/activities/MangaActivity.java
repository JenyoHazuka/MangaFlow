package com.example.mangaflow.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mangaflow.R;
import com.example.mangaflow.utils.MangaClass;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MangaActivity extends AppCompatActivity {

    private MangaClass currentManga;
    private Button btnAjouter, btnSuivre, btnALire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga);

        // 1. Initialisation des boutons
        btnAjouter = findViewById(R.id.btn_add);
        btnSuivre = findViewById(R.id.btn_follow);
        btnALire = findViewById(R.id.btn_read);

        // 2. Récupération des extras (Titre ET Numéro)
        String targetTitle = getIntent().getStringExtra("TITRE_MANGA");
        int targetNumber = getIntent().getIntExtra("NUMERO_TOME", 1);

        if (targetTitle != null) {
            loadMangaFromJSON(targetTitle, targetNumber);
        } else {
            Toast.makeText(this, "Erreur : aucun manga spécifié", Toast.LENGTH_SHORT).show();
            finish();
        }

        // --- NAVIGATION ---
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_planning).setOnClickListener(v -> startActivity(new Intent(this, PlanningActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
        setupSerieLinks();

        // --- LISTENERS ACTIONS ---
        btnAjouter.setOnClickListener(v -> handleAction("ADD"));
        btnSuivre.setOnClickListener(v -> handleAction("FOLLOW"));
        btnALire.setOnClickListener(v -> handleAction("READ"));
    }

    private void handleAction(String actionType) {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String email = pref.getString("user_email", null);

        if (email == null) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        try {
            File file = new File(getFilesDir(), email + "_data.json");
            JSONObject userData = file.exists() ? new JSONObject(loadStringFromFile(file)) : new JSONObject();
            if (!userData.has("collection")) userData.put("collection", new JSONArray());

            JSONArray collection = userData.getJSONArray("collection");
            JSONObject serie = findOrCreateSerie(collection, currentManga.getTitre_serie());
            JSONArray mangasDeLaSerie = serie.getJSONArray("mangas");
            JSONObject tomeActuel = findOrCreateTome(mangasDeLaSerie, currentManga.getNumero_tome());

            switch (actionType) {
                case "ADD":
                    boolean isPossede = !tomeActuel.optBoolean("posséder", false);
                    tomeActuel.put("posséder", isPossede);

                    if (isPossede) {
                        // 1. On active le suivi sur le tome cliqué
                        tomeActuel.put("souhaiter", true);

                        // 2. LOGIQUE GLOBALE : On s'assure que TOUS les tomes de la série sont créés et suivis
                        // On récupère le nombre total de tomes depuis l'objet série (que findOrCreateSerie a rempli)
                        int totalTomes = serie.optInt("nombre_tome_total", 0);

                        for (int n = 1; n <= totalTomes; n++) {
                            // Cette méthode va soit trouver le tome existant, soit le créer
                            JSONObject t = findOrCreateTome(mangasDeLaSerie, n);
                            t.put("souhaiter", true);
                        }
                        Log.d("MangaFlow", "Suivi activé pour l'intégralité des " + totalTomes + " tomes de " + currentManga.getTitre_serie());
                    }
                    break;

                case "FOLLOW":
                    tomeActuel.put("souhaiter", !tomeActuel.optBoolean("souhaiter", false));
                    break;

                case "READ":
                    tomeActuel.put("lu", !tomeActuel.optBoolean("lu", false));
                    break;
            }

            saveStringToFile(file, userData.toString());
            updateButtonsUI(tomeActuel); // Rafraîchit le bouton de la page actuelle

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-vérifie le fichier JSON utilisateur pour mettre à jour les boutons
        checkAndRefreshUserStatus();
    }

    private void updateButtonsUI(JSONObject tome) {
        // runOnUiThread garantit que l'affichage change bien sur l'écran
        runOnUiThread(() -> {
            try {
                // AJOUTER / RETIRER
                boolean possede = tome.optBoolean("posséder", false);
                btnAjouter.setBackgroundTintList(ColorStateList.valueOf(possede ? Color.parseColor("#0083ff") : Color.WHITE));
                btnAjouter.setTextColor(possede ? Color.WHITE : Color.BLACK);
                btnAjouter.setText(possede ? "Retirer" : "Ajouter");

                // SUIVRE / SUIVI
                boolean souhaite = tome.optBoolean("souhaiter", false);
                btnSuivre.setBackgroundTintList(ColorStateList.valueOf(souhaite ? Color.parseColor("#C00F0C") : Color.WHITE));
                btnSuivre.setTextColor(souhaite ? Color.WHITE : Color.BLACK);
                btnSuivre.setText(souhaite ? "Suivi" : "Suivre");

                // LU / A LIRE
                boolean lu = tome.optBoolean("lu", false);
                btnALire.setBackgroundTintList(ColorStateList.valueOf(lu ? Color.parseColor("#009951") : Color.WHITE));
                btnALire.setTextColor(lu ? Color.WHITE : Color.BLACK);
                btnALire.setText(lu ? "Lu" : "A Lire");

            } catch (Exception e) {
                Log.e("MangaFlow", "Erreur rafraîchissement visuel boutons", e);
            }
        });
    }

    private void checkAndRefreshUserStatus() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String email = pref.getString("user_email", null);
        if (email == null || currentManga == null) return;

        try {
            File file = new File(getFilesDir(), email + "_data.json");
            if (!file.exists()) { resetButtonsToDefault(); return; }

            JSONObject userData = new JSONObject(loadStringFromFile(file));
            JSONArray collection = userData.optJSONArray("collection");
            if (collection == null) return;

            // ÉTAPE MANQUANTE : Trouver la bonne série dans la collection
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);

                // On vérifie si c'est la série du manga actuel
                if (serie.getString("nom").equalsIgnoreCase(currentManga.getTitre_serie())) {

                    // On récupère le tableau des mangas pour CETTE série
                    JSONArray mangas = serie.getJSONArray("mangas");

                    // Maintenant on peut faire la boucle sur les tomes
                    for (int j = 0; j < mangas.length(); j++) {
                        JSONObject tome = mangas.getJSONObject(j);

                        int numInJson = tome.optInt("numéro", -1);
                        int numCible = currentManga.getNumero_tome();

                        if (numInJson == numCible) {
                            updateButtonsUI(tome); // Met à jour les couleurs
                            return;
                        }
                    }
                }
            }

            // Si on arrive ici, c'est que le tome n'est pas encore dans le JSON
            resetButtonsToDefault();

        } catch (Exception e) {
            Log.e("MangaFlow", "Erreur refresh status", e);
        }
    }

    private void resetButtonsToDefault() {
        runOnUiThread(() -> {
            btnAjouter.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnAjouter.setTextColor(Color.BLACK);
            btnAjouter.setText("Ajouter");
            btnSuivre.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnSuivre.setTextColor(Color.BLACK);
            btnSuivre.setText("Suivre");
            btnALire.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnALire.setTextColor(Color.BLACK);
            btnALire.setText("A Lire");
        });
    }

    // --- UTILS JSON ---

    private JSONObject findOrCreateSerie(JSONArray collection, String nom) throws Exception {
        for (int i = 0; i < collection.length(); i++) {
            if (collection.getJSONObject(i).getString("nom").equalsIgnoreCase(nom)) return collection.getJSONObject(i);
        }
        int totalTomes = 0;
        try {
            InputStream is = getResources().openRawResource(R.raw.series);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            JSONArray allSeries = new JSONArray(new String(buffer, "UTF-8"));
            for (int i = 0; i < allSeries.length(); i++) {
                if (allSeries.getJSONObject(i).getString("nom").equalsIgnoreCase(nom)) {
                    totalTomes = allSeries.getJSONObject(i).getInt("nombre_tome_total");
                    break;
                }
            }
        } catch (Exception e) { Log.e("MangaFlow", "Erreur lecture series.json"); }

        JSONObject newSerie = new JSONObject();
        newSerie.put("nom", nom);
        newSerie.put("nombre_tome_total", totalTomes);
        newSerie.put("mangas", new JSONArray());
        collection.put(newSerie);
        return newSerie;
    }

    private JSONObject findOrCreateTome(JSONArray mangas, int numero) throws Exception {
        for (int i = 0; i < mangas.length(); i++) {
            if (mangas.getJSONObject(i).optInt("numéro") == numero) return mangas.getJSONObject(i);
        }

        // Vérifier si au moins un autre tome de cette série est déjà suivi
        boolean serieDejaSuivie = false;
        for (int i = 0; i < mangas.length(); i++) {
            if (mangas.getJSONObject(i).optBoolean("souhaiter", false)) {
                serieDejaSuivie = true;
                break;
            }
        }

        JSONObject newTome = new JSONObject();
        newTome.put("numéro", numero);
        newTome.put("jaquette", currentManga.getImage_url());
        newTome.put("lu", false);
        // Si la série est déjà suivie, le nouveau tome l'est aussi automatiquement
        newTome.put("souhaiter", serieDejaSuivie);
        newTome.put("posséder", false);
        mangas.put(newTome);
        return newTome;
    }

    private String loadStringFromFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data); fis.close();
        return new String(data, StandardCharsets.UTF_8);
    }

    private void saveStringToFile(File file, String content) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    private void loadMangaFromJSON(String titleToFind, int numberToFind) {
        try {
            InputStream is = getResources().openRawResource(R.raw.mangas);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            JSONArray jsonArray = new JSONArray(new String(buffer, StandardCharsets.UTF_8));

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                // FILTRE SUR LE TITRE ET LE NUMÉRO DE TOME
                if (obj.optString("titre_serie").equalsIgnoreCase(titleToFind) &&
                        obj.optInt("numero_tome") == numberToFind) {

                    List<String> auteurs = new ArrayList<>();
                    JSONArray auteursJ = obj.optJSONArray("auteurs");
                    if (auteursJ != null) {
                        for(int j=0; j<auteursJ.length(); j++) auteurs.add(auteursJ.getString(j));
                    }

                    // Traitement du prix (Nettoyage de la chaîne "€" et ",")
                    String prixRaw = obj.optString("prix", "0.0");
                    float prixClean = 0.0f;
                    try {
                        prixClean = Float.parseFloat(prixRaw.replace("€", "").replace(",", ".").trim());
                    } catch (Exception e) { Log.e("MangaFlow", "Erreur prix"); }

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
                            new ArrayList<>(),
                            obj.optString("resume"),
                            false, false, false
                    );

                    updateUI(currentManga);
                    checkAndRefreshUserStatus();
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("MangaFlow", "Erreur JSON", e);
        }
    }

    private void setupSerieLinks() {
        findViewById(R.id.tv_serie_name).setOnClickListener(v -> {
            if (currentManga != null) {
                Intent intent = new Intent(this, SerieActivity.class);
                intent.putExtra("SERIE_NAME", currentManga.getTitre_serie());
                intent.putExtra("NUMERO_TOME", currentManga.getNumero_tome());
                intent.putExtra("EDITEUR_NAME", currentManga.getEditeur());
                startActivity(intent);
            }
        });
    }

    private void updateUI(MangaClass manga) {
        ((TextView) findViewById(R.id.tv_header_title)).setText(manga.getTitre_serie());
        ((TextView) findViewById(R.id.tv_info_title)).setText(manga.getTitre_serie());
        ((TextView) findViewById(R.id.tv_info_tome)).setText("Tome " + manga.getNumero_tome());
        ((TextView) findViewById(R.id.tv_serie_name)).setText(manga.getTitre_serie());
        ((TextView) findViewById(R.id.tv_edition_name)).setText(manga.getEdition() + " - " + manga.getEditeur());
        ((TextView) findViewById(R.id.tv_manga_summary)).setText(manga.getResume());

        // FIX PRIX ET PAGES
        ((TextView) findViewById(R.id.tv_price)).setText(String.format("%.2f€", manga.getPrix()));
        ((TextView) findViewById(R.id.tv_nb_pages)).setText("Nombre de pages : " + (manga.getNb_pages() > 0 ? manga.getNb_pages() : "Inconnu"));
        ((TextView) findViewById(R.id.tv_date)).setText(manga.getDate_parution());
        ((TextView) findViewById(R.id.tv_isbn)).setText(manga.getIsbn());

        ImageView cover = findViewById(R.id.iv_manga_cover);
        Glide.with(this).load(manga.getImage_url()).centerCrop().into(cover);
    }
}