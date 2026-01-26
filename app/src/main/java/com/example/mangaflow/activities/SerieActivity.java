package com.example.mangaflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.utils.MangaAdapter;
import com.example.mangaflow.utils.MangaClass;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SerieActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<MangaClass> tomesDeLEdition = new ArrayList<>();
    private MangaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serie);

        String sName = getIntent().getStringExtra("SERIE_NAME");
        String eName = getIntent().getStringExtra("EDITEUR_NAME");

        if (sName == null) { finish(); return; }

        TextView tvTitle = findViewById(R.id.tv_serie_title_header);
        if (tvTitle != null) tvTitle.setText(sName);

        recyclerView = findViewById(R.id.rv_tomes_carrousel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // --- NAVIGATION ---
        findViewById(R.id.btn_back_serie).setOnClickListener(v -> finish());
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_planning).setOnClickListener(v -> startActivity(new Intent(this, PlanningActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));

        loadTomes(sName, eName);
    }

    private void loadTomes(String serie, String editeur) {
        try {
            // PARTIE 1 : RÉCUPÉRATION DEPUIS SERIES.JSON
            InputStream isSeries = getResources().openRawResource(R.raw.series);
            byte[] bufferS = new byte[isSeries.available()];
            isSeries.read(bufferS);
            isSeries.close();

            JSONArray arraySeries = new JSONArray(new String(bufferS, StandardCharsets.UTF_8));

            for (int i = 0; i < arraySeries.length(); i++) {
                JSONObject obj = arraySeries.getJSONObject(i);
                if (obj.optString("titre").equalsIgnoreCase(serie)) {

                    // Extraction de l'auteur et mise en place de la redirection
                    JSONArray auteursJ = obj.optJSONArray("auteurs");
                    if (auteursJ != null && auteursJ.length() > 0) {
                        JSONObject auteurObj = auteursJ.getJSONObject(0);
                        String nomAuteur = auteurObj.optString("nom", "Auteur inconnu");

                        TextView tvAuteur = findViewById(R.id.tv_serie_auteur);
                        if (tvAuteur != null) tvAuteur.setText(nomAuteur);

                        // MODIFICATION : REDIRECTION BASÉE SUR LE CONTENU DU CHAMP
                        View layoutAuteur = findViewById(R.id.layout_auteur_cliquable);
                        if (layoutAuteur != null && tvAuteur != null) {
                            layoutAuteur.setOnClickListener(v -> {
                                // On récupère le texte directement du TextView au moment du clic
                                String nomAEnvoyer = tvAuteur.getText().toString().trim();

                                Intent intent = new Intent(SerieActivity.this, AuteurActivity.class);
                                intent.putExtra("nom_auteur", nomAEnvoyer);
                                startActivity(intent);
                            });
                        }
                    }

                    // Extraction nb_tomes et statut
                    JSONArray editionsJ = obj.optJSONArray("editions");
                    if (editionsJ != null && editionsJ.length() > 0) {
                        JSONObject editObj = editionsJ.getJSONObject(0);
                        String nb = editObj.optString("nb_tomes", "X");
                        String statut = editObj.optString("statut", "en cours");

                        TextView tvStatus = findViewById(R.id.tv_serie_status_info);
                        if (tvStatus != null) {
                            tvStatus.setText(nb + " parus - Édition " + statut);
                        }
                    }
                    break;
                }
            }

            // PARTIE 2 : RÉCUPÉRATION DES TOMES DEPUIS MANGAS.JSON
            InputStream isMangas = getResources().openRawResource(R.raw.mangas);
            byte[] bufferM = new byte[isMangas.available()];
            isMangas.read(bufferM);
            isMangas.close();

            JSONArray arrayMangas = new JSONArray(new String(bufferM, StandardCharsets.UTF_8));
            tomesDeLEdition.clear();

            for (int i = 0; i < arrayMangas.length(); i++) {
                JSONObject obj = arrayMangas.getJSONObject(i);
                if (obj.optString("titre_serie").equalsIgnoreCase(serie) &&
                        obj.optString("editeur_fr").equalsIgnoreCase(editeur)) {

                    tomesDeLEdition.add(new MangaClass(
                            obj.optString("titre_serie"),
                            obj.optInt("numero_tome"),
                            obj.optString("image_url"),
                            obj.optString("edition"),
                            obj.optString("ean"),
                            obj.optString("editeur_fr"),
                            "", 0.0f, 0, null, null, "",
                            false, false, false
                    ));
                }
            }

            adapter = new MangaAdapter(tomesDeLEdition, R.layout.manga_item_carrousel);
            recyclerView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}