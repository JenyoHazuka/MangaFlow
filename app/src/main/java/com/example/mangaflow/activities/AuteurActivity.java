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

        tvNomAuteur = findViewById(R.id.tv_nom_auteur);
        rvOeuvres = findViewById(R.id.rv_oeuvres_auteur);

        if (tvNomAuteur == null) { finish(); return; }

        // RÉCUPÉRATION DU JSON COMPLET
        String jsonStr = getIntent().getStringExtra("DATA_JSON");

        if (jsonStr != null) {
            try {
                JSONObject dataAuteur = new JSONObject(jsonStr);
                String nomAuteur = dataAuteur.getString("nom");

                // Nettoyage pour l'affichage
                tvNomAuteur.setText(nomAuteur.split("\\(")[0].trim());

                // Extraction directe de la jointure depuis l'objet reçu
                JSONArray jointure = dataAuteur.optJSONArray("oeuvres_jointure");
                List<String> titresOeuvres = new ArrayList<>();
                if (jointure != null) {
                    for (int i = 0; i < jointure.length(); i++) {
                        titresOeuvres.add(jointure.getString(i));
                    }
                }
                chargerImagesDepuisMangas(titresOeuvres);

            } catch (Exception e) { Log.e(TAG, "Erreur JSON", e); }
        }

        rvOeuvres.setLayoutManager(new GridLayoutManager(this, 2));
        rvOeuvres.setAdapter(new AuteurOeuvreAdapter(listeOeuvres));

        setupNavigation();
    }

    private void chargerImagesDepuisMangas(List<String> titres) {
        try {
            InputStream is = getResources().openRawResource(R.raw.mangas);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONArray mangasArray = new JSONArray(new String(buffer, StandardCharsets.UTF_8));

            for (String titre : titres) {
                for (int i = 0; i < mangasArray.length(); i++) {
                    JSONObject manga = mangasArray.getJSONObject(i);
                    if (manga.optString("titre_serie").equalsIgnoreCase(titre)) {
                        listeOeuvres.add(manga);
                        break;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupNavigation() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }
}