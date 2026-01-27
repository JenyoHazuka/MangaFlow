package com.example.mangaflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
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

public class SerieActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private List<MangaClass> tomesDeLEdition = new ArrayList<>();
    private String sName = "";
    private String eName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serie);

        String jsonStr = getIntent().getStringExtra("DATA_JSON");
        try {
            if (jsonStr != null) {
                JSONObject data = new JSONObject(jsonStr);
                sName = data.getString("titre");
            } else {
                sName = getIntent().getStringExtra("SERIE_NAME");
                eName = getIntent().getStringExtra("EDITEUR_NAME");
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (sName == null || sName.isEmpty()) { finish(); return; }

        TextView tvTitle = findViewById(R.id.tv_serie_title_header);
        if (tvTitle != null) tvTitle.setText(sName);

        recyclerView = findViewById(R.id.rv_tomes_carrousel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        findViewById(R.id.btn_back_serie).setOnClickListener(v -> finish());
        loadTomes(sName, eName);
    }

    private void loadTomes(String serie, String editeur) {
        try {
            // Lecture Series.json pour les infos header
            InputStream isS = getResources().openRawResource(R.raw.series);
            byte[] bufferS = new byte[isS.available()];
            isS.read(bufferS);
            isS.close();
            JSONArray arraySeries = new JSONArray(new String(bufferS, StandardCharsets.UTF_8));

            for (int i = 0; i < arraySeries.length(); i++) {
                JSONObject obj = arraySeries.getJSONObject(i);
                if (obj.optString("titre").equalsIgnoreCase(serie)) {
                    // Maj Auteur
                    JSONArray auteurs = obj.optJSONArray("auteurs");
                    if (auteurs != null && auteurs.length() > 0) {
                        String nomAut = auteurs.getJSONObject(0).optString("nom");
                        ((TextView)findViewById(R.id.tv_serie_auteur)).setText(nomAut);
                        findViewById(R.id.layout_auteur_cliquable).setOnClickListener(v -> {
                            Intent intent = new Intent(this, AuteurActivity.class);
                            intent.putExtra("nom_auteur", nomAut);
                            startActivity(intent);
                        });
                    }
                    break;
                }
            }

            // Lecture Mangas.json pour les tomes
            InputStream isM = getResources().openRawResource(R.raw.mangas);
            byte[] bufferM = new byte[isM.available()];
            isM.read(bufferM);
            isM.close();
            JSONArray arrayMangas = new JSONArray(new String(bufferM, StandardCharsets.UTF_8));

            tomesDeLEdition.clear();
            for (int i = 0; i < arrayMangas.length(); i++) {
                JSONObject m = arrayMangas.getJSONObject(i);
                if (m.optString("titre_serie").equalsIgnoreCase(serie)) {
                    if (editeur == null || m.optString("editeur_fr").equalsIgnoreCase(editeur)) {
                        tomesDeLEdition.add(
                                new MangaClass(m.optString("titre_serie"),
                                        m.optInt("numero_tome"),
                                        m.optString("image_url"),
                                        m.optString("edition"),
                                        m.optString("ean"),
                                        m.optString("editeur_fr"),
                                        "", 0.00f, 0, null,
                                        null, "", false, false,
                                        false));
                    }
                }
            }
            recyclerView.setAdapter(new MangaAdapter(tomesDeLEdition, R.layout.manga_item_carrousel));
        } catch (Exception e) { e.printStackTrace(); }
    }
}