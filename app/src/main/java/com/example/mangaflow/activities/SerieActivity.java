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

/**
 * Activité affichant la fiche détaillée d'une série.
 * Elle regroupe les informations sur l'auteur, l'édition et affiche la liste des tomes.
 */
public class SerieActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private List<MangaClass> tomesDeLEdition = new ArrayList<>();
    private String sName = ""; // Nom de la série
    private String eName = ""; // Nom de l'éditeur

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serie);

        // 1. RÉCUPÉRATION DES PARAMÈTRES : Tentative de récupération via JSON brut ou Extras simples
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

        // Sécurité : si on n'a pas de nom de série, on ne peut rien afficher
        if (sName == null || sName.isEmpty()) {
            finish();
            return;
        }

        // Mise à jour du titre dans l'en-tête (Header)
        TextView tvTitle = findViewById(R.id.tv_serie_title_header);
        if (tvTitle != null) tvTitle.setText(sName);

        // 2. CONFIGURATION DU RECYCLERVIEW : Utilisation d'un layout horizontal pour l'effet carrousel
        recyclerView = findViewById(R.id.rv_tomes_carrousel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // --- NAVIGATION : Gestion des clics sur les boutons de retour et du menu principal ---
        findViewById(R.id.btn_back_serie).setOnClickListener(v -> finish());
        setupNavigationListeners();

        // 3. CHARGEMENT DES DONNÉES : On lance la logique de lecture des fichiers JSON
        loadTomes(sName, eName);
    }

    /**
     * Orchestre le chargement des informations de la série (Auteur, Statut) et de ses tomes.
     */
    private void loadTomes(String serie, String editeur) {
        try {
            // ÉTAPE 1 : Si l'éditeur est inconnu, on le cherche dans editeurs.json en parcourant les listes de mangas
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

            // ÉTAPE 2 : Recherche des métadonnées (Auteur, Éditions, Statut) dans series.json
            InputStream isS = getResources().openRawResource(R.raw.series);
            byte[] bufferS = new byte[isS.available()];
            isS.read(bufferS);
            isS.close();
            JSONArray arraySeries = new JSONArray(new String(bufferS, StandardCharsets.UTF_8));

            for (int i = 0; i < arraySeries.length(); i++) {
                JSONObject obj = arraySeries.getJSONObject(i);
                String titreJson = obj.has("titre") ? obj.getString("titre") : obj.optString("nom");

                if (titreJson.equalsIgnoreCase(serie)) {
                    // Extraction et affichage du premier auteur trouvé
                    JSONArray auteursArray = obj.optJSONArray("auteurs");
                    if (auteursArray != null && auteursArray.length() > 0) {
                        final String nomAut = auteursArray.getJSONObject(0).optString("nom");
                        ((TextView) findViewById(R.id.tv_serie_auteur)).setText(nomAut);

                        // Rendre le nom de l'auteur cliquable pour aller sur sa fiche
                        findViewById(R.id.layout_auteur_cliquable).setOnClickListener(v -> {
                            Intent intent = new Intent(this, AuteurActivity.class);
                            intent.putExtra("nom_auteur", nomAut);
                            startActivity(intent);
                        });
                    }

                    // Extraction des détails de l'édition (Nombre de tomes et statut)
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

            // ÉTAPE 3 : Chargement de la liste des tomes pour remplir le carrousel
            loadMangasCarrousel(serie, editeur);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si une série appartient à un éditeur donné en cherchant dans ses différentes catégories.
     */
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

    /**
     * Parcourt mangas.json pour récupérer tous les tomes appartenant à la série actuelle.
     */
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
                // On vérifie le titre de la série et éventuellement l'éditeur
                if (m.optString("titre_serie").equalsIgnoreCase(serie)) {
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

            // TRI : On s'assure que les tomes apparaissent dans l'ordre (Tome 1, 2, 3...)
            tomesDeLEdition.sort((m1, m2) -> Integer.compare(m1.getNumero_tome(), m2.getNumero_tome()));

            // Attribution de l'adapter avec un layout spécifique pour les items du carrousel
            recyclerView.setAdapter(new MangaAdapter(tomesDeLEdition, R.layout.manga_item_carrousel));

        } catch (Exception e) {
            Log.e("MangaFlow", "Erreur carrousel mangas", e);
        }
    }

    /**
     * Centralisation des listeners de navigation.
     */
    private void setupNavigationListeners() {
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_planning).setOnClickListener(v -> startActivity(new Intent(this, PlanningActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
    }
}