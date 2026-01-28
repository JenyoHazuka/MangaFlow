package com.example.mangaflow.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangaflow.utils.MangaAdapter;
import com.example.mangaflow.models.MangaClass;
import com.example.mangaflow.R;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activité d'accueil (Dashboard).
 * Affiche les sorties récentes et sert de pivot central pour la navigation.
 */
public class HomeActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private MangaAdapter adapter;
    private List<MangaClass> mangaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Configuration de la grille : 2 colonnes pour les couvertures de mangas
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 1. VÉRIFICATION RÉSEAU : On ne charge les données que si l'appareil est connecté
        if (isNetworkAvailable()) {
            // Initialisation de la liste des données via le fichier JSON local
            mangaList = loadMangasFromJSON();

            // Initialisation de l'adapter (le pont entre les données et la vue)
            adapter = new MangaAdapter(mangaList, R.layout.manga_item);

            // Liaison de l'adapter au RecyclerView
            recyclerView.setAdapter(adapter);

            // Notification pour forcer le rafraîchissement visuel
            adapter.notifyDataSetChanged();
        } else {
            // Feedback utilisateur en cas d'absence de réseau
            Toast.makeText(this, "Pas de connexion internet", Toast.LENGTH_LONG).show();
        };

        // LIAISON UI : Récupération des icônes de la barre de navigation
        ImageView btnPlanning = findViewById(R.id.btn_planning);
        ImageView btnCollection = findViewById(R.id.btn_collection);
        ImageView btnSearch = findViewById(R.id.btn_search);
        ImageView btnMaps = findViewById(R.id.btn_maps);
        ImageView btnAccount = findViewById(R.id.btn_account);

        // GESTION DU COMPTE (Login / Logout)
        btnAccount.setOnClickListener(v -> {
            // On vérifie si un utilisateur est déjà en session
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            String userEmail = pref.getString("user_email", null);

            if (userEmail != null) {
                // Connecté -> Direction la page de déconnexion/profil
                Intent intent = new Intent(HomeActivity.this, LogoutActivity.class);
                startActivity(intent);
            } else {
                // Déconnecté -> Direction la page de connexion
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // 2. FILTRAGE : Chargement spécifique des sorties déjà parues (Home)
        chargerMangasHome();

        // --- NAVIGATION VERS LES AUTRES MODULES AVEC TRANSITIONS ---

        btnPlanning.setOnClickListener(v -> {
            Intent PlanningIntent = new Intent(HomeActivity.this, PlanningActivity.class);
            startActivity(PlanningIntent);
            // Animation personnalisée : glissement de gauche à droite
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnCollection.setOnClickListener(v -> {
            Intent CollectionIntent = new Intent(HomeActivity.this, CollectionActivity.class);
            startActivity(CollectionIntent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnSearch.setOnClickListener(v -> {
            Intent SearchIntent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(SearchIntent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnMaps.setOnClickListener(v -> {
            Intent MapsIntent = new Intent(HomeActivity.this, MapsActivity.class);
            startActivity(MapsIntent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    /**
     * Vérifie si le Wi-Fi ou les données mobiles sont activés.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Parse le fichier raw/mangas.json pour transformer le texte brut en objets Java.
     */
    private List<MangaClass> loadMangasFromJSON() {
        List<MangaClass> list = new ArrayList<>();
        try {
            // Lecture physique du fichier binaire
            InputStream is = getResources().openRawResource(R.raw.mangas);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            // Conversion de la chaîne en tableau JSON
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                // Mapping sécurisé (utilise des valeurs par défaut si une clé manque)
                String titre = obj.optString("titre_serie", "Inconnu");
                int tome = obj.optInt("numero_tome", 0);
                String img = obj.optString("image_url", "");
                String edit = obj.optString("edition", "");
                String isbn = obj.optString("ean", "0000000000000"); // Identifiant unique
                String editeur = obj.optString("editeur_fr", "");
                String date = obj.optString("date_parution", "");
                String resume = obj.optString("resume", "");
                Boolean lu = obj.optBoolean("lu", false);
                Boolean suivi = obj.optBoolean("suivi", false);
                Boolean possede = obj.optBoolean("possede", false);

                // Création du modèle MangaClass
                list.add(new MangaClass(
                        titre, tome, img, edit, isbn, editeur, date,
                        0.0f, 0, null, null, resume,
                        lu, suivi, possede
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Filtre les données pour n'afficher que les 50 derniers mangas déjà sortis en librairie.
     */
    private void chargerMangasHome() {
        try {
            InputStream is = getResources().openRawResource(R.raw.mangas);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONArray array = new JSONArray(new String(buffer, StandardCharsets.UTF_8));
            List<MangaClass> tempListe = new ArrayList<>();

            // Préparation du comparateur de date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            Date aujourdhui = new Date();

            // PARCOURS INVERSÉ : On commence par la fin du JSON (souvent les sorties les plus récentes)
            for (int i = array.length() - 1; i >= 0; i--) {
                // Limitation arbitraire pour éviter de surcharger la page d'accueil
                if (tempListe.size() >= 50) break;

                JSONObject obj = array.getJSONObject(i);
                String dateSortieStr = obj.optString("date_parution", "");

                try {
                    Date dateSortie = sdf.parse(dateSortieStr);

                    // LOGIQUE : On affiche uniquement si le manga est DÉJÀ disponible (date <= aujourd'hui)
                    if (dateSortie != null && !dateSortie.after(aujourdhui)) {
                        tempListe.add(new MangaClass(
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
                } catch (java.text.ParseException e) {
                    Log.e("HOME_DATE", "Format de date invalide pour : " + dateSortieStr);
                }
            }

            // MISE À JOUR : On vide la liste actuelle pour la remplacer par la liste filtrée
            mangaList.clear();
            mangaList.addAll(tempListe);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e("HOME_ERROR", "Erreur lors du chargement : " + e.getMessage());
        }
    }
}