package com.example.mangaflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.utils.MangaAdapter;
import com.example.mangaflow.models.MangaClass;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SerieActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private List<MangaClass> tomesDeLEdition = new ArrayList<>();
    private String sName = "";
    private String eName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serie);

        // Récupération des données passées par l'Intent
        try {
            String jsonStr = getIntent().getStringExtra("DATA_JSON");
            if (jsonStr != null) {
                JSONObject data = new JSONObject(jsonStr);
                sName = data.optString("titre", data.optString("nom"));
            } else {
                sName = getIntent().getStringExtra("SERIE_NAME");
                eName = getIntent().getStringExtra("EDITEUR_NAME");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sName == null || sName.isEmpty()) {
            finish();
            return;
        }

        // Header Titre
        TextView tvTitle = findViewById(R.id.tv_serie_title_header);
        if (tvTitle != null) tvTitle.setText(sName);

        // Configuration du RecyclerView en Horizontal (Carrousel)
        recyclerView = findViewById(R.id.rv_tomes_carrousel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // --- NAVIGATION ---
        findViewById(R.id.btn_back_serie).setOnClickListener(v -> finish());

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

        // Lancement du chargement des données
        loadTomes(sName, eName);
    }

    private void loadTomes(String serie, String editeur) {
        try {
            // 1. CHERCHER L'ÉDITEUR DANS editeurs.json (si absent de l'Intent)
            if (editeur == null || editeur.isEmpty()) {
                InputStream isE = getResources().openRawResource(R.raw.editeurs);
                byte[] bufferE = new byte[isE.available()];
                isE.read(bufferE);
                isE.close();
                JSONArray arrayEditeurs = new JSONArray(new String(bufferE, StandardCharsets.UTF_8));

                for (int i = 0; i < arrayEditeurs.length(); i++) {
                    JSONObject editObj = arrayEditeurs.getJSONObject(i);
                    if (isSerieInEditeur(editObj, serie)) {
                        editeur = editObj.optString("nom");
                        break;
                    }
                }
            }

            // 2. CHERCHER LES INFOS DANS series.json (Auteur, Editions, Statut)
            InputStream isS = getResources().openRawResource(R.raw.series);
            byte[] bufferS = new byte[isS.available()];
            isS.read(bufferS);
            isS.close();
            JSONArray arraySeries = new JSONArray(new String(bufferS, StandardCharsets.UTF_8));

            for (int i = 0; i < arraySeries.length(); i++) {
                JSONObject obj = arraySeries.getJSONObject(i);
                String titreJson = obj.has("titre") ? obj.getString("titre") : obj.optString("nom");

                if (titreJson.equalsIgnoreCase(serie)) {
                    // --- PARTIE AUTEUR ---
                    JSONArray auteursArray = obj.optJSONArray("auteurs");
                    if (auteursArray != null && auteursArray.length() > 0) {
                        final String nomAut = auteursArray.getJSONObject(0).optString("nom");
                        ((TextView) findViewById(R.id.tv_serie_auteur)).setText(nomAut);
                        findViewById(R.id.layout_auteur_cliquable).setOnClickListener(v -> {
                            Intent intent = new Intent(this, AuteurActivity.class);
                            intent.putExtra("nom_auteur", nomAut);
                            startActivity(intent);
                        });
                    }

                    // --- PARTIE ÉDITIONS (Structure imbriquée de ton JSON) ---
                    JSONArray editionsArray = obj.optJSONArray("editions");
                    if (editionsArray != null && editionsArray.length() > 0) {
                        JSONObject editionObj = editionsArray.getJSONObject(0);

                        String nomEdition = editionObj.optString("nom_edition", "Standard");
                        String nbTomes = editionObj.optString("nb_tomes", "0 tome");
                        String statut = editionObj.optString("statut", "En cours");

                        ((TextView) findViewById(R.id.tv_serie_edition_details)).setText(nomEdition + " - " + (editeur != null ? editeur : "Inconnu"));
                        ((TextView) findViewById(R.id.tv_serie_status_info)).setText(nbTomes + " - Édition " + statut);
                    }
                    break;
                }
            }

            // 3. CHARGER LES TOMES DANS LE CARROUSEL
            loadMangasCarrousel(serie, editeur);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isSerieInEditeur(JSONObject editeurJson, String serieNom) {
        String[] categories = {"mangas", "light_novels", "artbooks"};
        for (String cat : categories) {
            JSONArray list = editeurJson.optJSONArray(cat);
            if (list != null) {
                for (int i = 0; i < list.length(); i++) {
                    if (list.optString(i).equalsIgnoreCase(serieNom)) return true;
                }
            }
        }
        return false;
    }

    private void loadMangasCarrousel(String serie, String editeur) {
        try {
            InputStream isM = getResources().openRawResource(R.raw.mangas);
            byte[] bufferM = new byte[isM.available()];
            isM.read(bufferM);
            isM.close();
            JSONArray arrayMangas = new JSONArray(new String(bufferM, StandardCharsets.UTF_8));

            tomesDeLEdition.clear();
            for (int i = 0; i < arrayMangas.length(); i++) {
                JSONObject m = arrayMangas.getJSONObject(i);
                if (m.optString("titre_serie").equalsIgnoreCase(serie)) {
                    // Filtrage optionnel par éditeur si présent
                    if (editeur == null || editeur.isEmpty() || m.optString("editeur_fr").equalsIgnoreCase(editeur)) {
                        tomesDeLEdition.add(new MangaClass(
                                m.optString("titre_serie"),
                                m.optInt("numero_tome"),
                                m.optString("image_url"),
                                m.optString("edition"),
                                m.optString("ean"),
                                m.optString("editeur_fr"),
                                "", 0.00f, 0, null, null, "", false, false, false
                        ));
                    }
                }
            }

            // Tri par numéro de tome (Java 8+)
            tomesDeLEdition.sort((m1, m2) -> Integer.compare(m1.getNumero_tome(), m2.getNumero_tome()));

            recyclerView.setAdapter(new MangaAdapter(tomesDeLEdition, R.layout.manga_item_carrousel));

        } catch (Exception e) {
            Log.e("MangaFlow", "Erreur carrousel mangas", e);
        }
    }
}