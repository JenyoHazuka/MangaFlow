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

/**
 * Activité affichant la fiche détaillée d'un auteur et la liste de ses œuvres.
 */
public class AuteurActivity extends BaseActivity {

    // Composants UI
    private RecyclerView rvOeuvres;
    private TextView tvNomAuteur;

    // Données
    private List<JSONObject> listeOeuvres = new ArrayList<>(); // Liste d'objets JSON représentant les mangas de l'auteur
    private String nomAuteurRecu = ""; // Nom de l'auteur récupéré depuis l'activité précédente
    private static final String TAG = "DEBUG_AUTEUR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auteur);

        // Initialisation des vues
        tvNomAuteur = findViewById(R.id.tv_nom_auteur);
        rvOeuvres = findViewById(R.id.rv_oeuvres_auteur);

        // Sécurité : si la vue principale n'est pas trouvée, on ferme l'activité
        if (tvNomAuteur == null) { finish(); return; }

        // 1. RÉCUPÉRATION DU NOM (Passé par SerieActivity ou SearchActivity via l'Intent)
        nomAuteurRecu = getIntent().getStringExtra("nom_auteur");

        if (nomAuteurRecu != null && !nomAuteurRecu.isEmpty()) {
            /* NETTOYAGE DU NOM :
               Certains noms arrivent avec des rôles, ex: "Haruba Negi (Auteur)".
               On découpe à la parenthèse et on enlève les espaces vides.
            */
            tvNomAuteur.setText(nomAuteurRecu.split("\\(")[0].trim());

            // 2. Lancement de la logique de récupération des données
            chargerOeuvresDeLAuteur(nomAuteurRecu);
        } else {
            Log.e(TAG, "Aucun nom d'auteur reçu dans l'intent");
        }

        // Configuration de la liste (RecyclerView) en mode Grille (2 colonnes)
        rvOeuvres.setLayoutManager(new GridLayoutManager(this, 2));
        rvOeuvres.setAdapter(new AuteurOeuvreAdapter(listeOeuvres));

        // Initialisation des clics de navigation
        setupNavigation();
    }

    /**
     * Scanne le fichier series.json pour identifier toutes les séries liées à cet auteur.
     * @param nomAuteurCible Le nom de l'auteur à rechercher.
     */
    private void chargerOeuvresDeLAuteur(String nomAuteurCible) {
        try {
            // A. Lecture du fichier RAW 'series.json'
            InputStream isS = getResources().openRawResource(R.raw.series);
            byte[] bufferS = new byte[isS.available()];
            isS.read(bufferS);
            isS.close();
            JSONArray seriesArray = new JSONArray(new String(bufferS, StandardCharsets.UTF_8));

            List<String> titresTrouves = new ArrayList<>();

            // Parcours de toutes les séries existantes
            for (int i = 0; i < seriesArray.length(); i++) {
                JSONObject serie = seriesArray.getJSONObject(i);
                JSONArray auteurs = serie.optJSONArray("auteurs");

                if (auteurs != null) {
                    // Une série peut avoir plusieurs auteurs (Scénariste + Dessinateur)
                    for (int j = 0; j < auteurs.length(); j++) {
                        String nomJson = auteurs.getJSONObject(j).optString("nom");
                        // Comparaison insensible à la casse pour trouver l'auteur cible
                        if (nomJson.equalsIgnoreCase(nomAuteurCible)) {
                            // Si trouvé, on mémorise le titre de la série
                            titresTrouves.add(serie.optString("nom", serie.optString("titre")));
                            break; // On sort de la boucle des auteurs pour cette série
                        }
                    }
                }
            }

            // B. Si l'auteur a des séries, on va chercher leurs couvertures
            if (!titresTrouves.isEmpty()) {
                chargerImagesDepuisMangas(titresTrouves);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du scan des séries", e);
        }
    }

    /**
     * Scanne le fichier mangas.json pour récupérer une image de couverture pour chaque série.
     * @param titres Liste des titres de séries trouvés pour l'auteur.
     */
    private void chargerImagesDepuisMangas(List<String> titres) {
        try {
            // Lecture du fichier RAW 'mangas.json'
            InputStream isM = getResources().openRawResource(R.raw.mangas);
            byte[] bufferM = new byte[isM.available()];
            isM.read(bufferM);
            isM.close();
            JSONArray mangasArray = new JSONArray(new String(bufferM, StandardCharsets.UTF_8));

            // Pour chaque titre de série identifié précédemment
            for (String titre : titres) {
                // On cherche dans la base globale des tomes
                for (int i = 0; i < mangasArray.length(); i++) {
                    JSONObject manga = mangasArray.getJSONObject(i);

                    // On cherche le premier tome trouvé (généralement le tome 1 ou le plus récent)
                    if (manga.optString("titre_serie").equalsIgnoreCase(titre)) {
                        /* On injecte manuellement la clé "nom" dans l'objet JSON.
                           Cela permet à l'AuteurOeuvreAdapter d'afficher le titre correctement.
                        */
                        manga.put("nom", titre);
                        listeOeuvres.add(manga); // Ajout à la liste d'affichage
                        break; // On s'arrête au premier trouvé pour ne pas avoir de doublons
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du scan des couvertures", e);
        }
    }

    /**
     * Initialise tous les boutons de la barre de navigation et le bouton retour.
     */
    private void setupNavigation() {
        // Bouton retour
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Boutons de la barre de menu inférieure
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_planning).setOnClickListener(v -> startActivity(new Intent(this, PlanningActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
    }
}