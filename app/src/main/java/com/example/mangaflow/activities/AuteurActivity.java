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
    private static final String TAG = "DEBUG_AUTEUR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auteur);

        // Initialisation sécurisée
        tvNomAuteur = findViewById(R.id.tv_nom_auteur);
        rvOeuvres = findViewById(R.id.rv_oeuvres_auteur);

        // TEST DE SÉCURITÉ : Si tvNomAuteur est null ici, ça va crasher au setText
        if (tvNomAuteur == null) {
            Log.e("DEBUG_AUTEUR", "ERREUR : tv_nom_auteur est introuvable dans le XML !");
            // On crée un TextView temporaire pour éviter le crash si l'ID est faux
            finish();
            return;
        }

        String nomAuteurCible = getIntent().getStringExtra("nom_auteur");

        if (nomAuteurCible != null) {
            // Nettoyage pour les noms avec parenthèses comme Abe Tsukasa
            String nomNettoye = nomAuteurCible.split("\\(")[0].trim();
            tvNomAuteur.setText(nomNettoye);
            chargerDonneesAuteur(nomNettoye);
        }

        rvOeuvres.setLayoutManager(new GridLayoutManager(this, 2));
        rvOeuvres.setAdapter(new AuteurOeuvreAdapter(listeOeuvres));

        // --- NAVIGATION ---
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_planning).setOnClickListener(v -> startActivity(new Intent(this, PlanningActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
    }

    private void chargerDonneesAuteur(String nomCible) {
        try {
            // Lecture auteurs.json
            InputStream isA = getResources().openRawResource(R.raw.auteurs);
            byte[] bufferA = new byte[isA.available()];
            isA.read(bufferA);
            isA.close();
            JSONArray auteursArray = new JSONArray(new String(bufferA, StandardCharsets.UTF_8));

            List<String> titresOeuvres = new ArrayList<>();
            for (int i = 0; i < auteursArray.length(); i++) {
                JSONObject aut = auteursArray.getJSONObject(i);
                if (aut.optString("nom").equalsIgnoreCase(nomCible)) {
                    JSONArray jointure = aut.optJSONArray("oeuvres_jointure");
                    if (jointure != null) {
                        for (int j = 0; j < jointure.length(); j++) {
                            titresOeuvres.add(jointure.getString(j));
                        }
                    }
                    break;
                }
            }

            Log.d(TAG, "Nombre d'oeuvres à chercher : " + titresOeuvres.size());

            // Lecture mangas.json
            InputStream isM = getResources().openRawResource(R.raw.mangas);
            byte[] bufferM = new byte[isM.available()];
            isM.read(bufferM);
            isM.close();
            JSONArray mangasArray = new JSONArray(new String(bufferM, StandardCharsets.UTF_8));

            for (String titre : titresOeuvres) {
                for (int k = 0; k < mangasArray.length(); k++) {
                    JSONObject manga = mangasArray.getJSONObject(k);
                    // Comparaison avec titre_serie
                    if (manga.optString("titre_serie").equalsIgnoreCase(titre)) {
                        listeOeuvres.add(manga);
                        break; // On prend le premier tome trouvé pour l'image
                    }
                }
            }
            Log.d(TAG, "Oeuvres ajoutées à la liste : " + listeOeuvres.size());

        } catch (Exception e) {
            Log.e(TAG, "Erreur JSON critique", e);
        }
    }
}