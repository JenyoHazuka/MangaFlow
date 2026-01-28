package com.example.mangaflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.utils.AuteurOeuvreAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AuteurActivity extends BaseActivity {

    private RecyclerView rvOeuvres;
    private TextView tvNomAuteur;
    private List<JSONObject> listeOeuvres = new ArrayList<>();
    private String nomAuteurRecu = "";
    private static final String TAG = "DEBUG_AUTEUR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auteur);

        tvNomAuteur = findViewById(R.id.tv_nom_auteur);
        rvOeuvres = findViewById(R.id.rv_oeuvres_auteur);

        if (tvNomAuteur == null) { finish(); return; }

        // 1. RÉCUPÉRATION DU NOM SIMPLE (Envoyé par SerieActivity)
        nomAuteurRecu = getIntent().getStringExtra("nom_auteur");

        if (nomAuteurRecu != null && !nomAuteurRecu.isEmpty()) {
            // Nettoyage du nom (ex: "Haruba Negi (Auteur)" -> "Haruba Negi")
            tvNomAuteur.setText(nomAuteurRecu.split("\\(")[0].trim());

            // 2. On charge les œuvres de cet auteur
            chargerOeuvresDeLAuteur(nomAuteurRecu);
        } else {
            Log.e(TAG, "Aucun nom d'auteur reçu dans l'intent");
        }

        rvOeuvres.setLayoutManager(new GridLayoutManager(this, 2));
        rvOeuvres.setAdapter(new AuteurOeuvreAdapter(listeOeuvres));

        setupNavigation();
    }

    private void chargerOeuvresDeLAuteur(String nomAuteurCible) {
        try {
            // A. On scanne series.json pour trouver les titres des séries de cet auteur
            InputStream isS = getResources().openRawResource(R.raw.series);
            byte[] bufferS = new byte[isS.available()];
            isS.read(bufferS);
            isS.close();
            JSONArray seriesArray = new JSONArray(new String(bufferS, StandardCharsets.UTF_8));

            List<String> titresTrouves = new ArrayList<>();

            for (int i = 0; i < seriesArray.length(); i++) {
                JSONObject serie = seriesArray.getJSONObject(i);
                JSONArray auteurs = serie.optJSONArray("auteurs");

                if (auteurs != null) {
                    for (int j = 0; j < auteurs.length(); j++) {
                        String nomJson = auteurs.getJSONObject(j).optString("nom");
                        if (nomJson.equalsIgnoreCase(nomAuteurCible)) {
                            // On ajoute le nom de la série (clé "nom" ou "titre")
                            titresTrouves.add(serie.optString("nom", serie.optString("titre")));
                            break;
                        }
                    }
                }
            }

            // B. Pour chaque titre trouvé, on va chercher une image de couverture dans mangas.json
            if (!titresTrouves.isEmpty()) {
                chargerImagesDepuisMangas(titresTrouves);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du scan des séries", e);
        }
    }

    private void chargerImagesDepuisMangas(List<String> titres) {
        try {
            InputStream isM = getResources().openRawResource(R.raw.mangas);
            byte[] bufferM = new byte[isM.available()];
            isM.read(bufferM);
            isM.close();
            JSONArray mangasArray = new JSONArray(new String(bufferM, StandardCharsets.UTF_8));

            for (String titre : titres) {
                for (int i = 0; i < mangasArray.length(); i++) {
                    JSONObject manga = mangasArray.getJSONObject(i);
                    // On prend le premier tome trouvé pour illustrer la série
                    if (manga.optString("titre_serie").equalsIgnoreCase(titre)) {
                        // On s'assure que l'objet a bien une clé "nom" pour l'adapter
                        manga.put("nom", titre);
                        listeOeuvres.add(manga);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du scan des couvertures", e);
        }
    }

    private void setupNavigation() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_planning).setOnClickListener(v -> startActivity(new Intent(this, PlanningActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
    }
}