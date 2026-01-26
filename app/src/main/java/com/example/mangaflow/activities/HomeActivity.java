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
import com.example.mangaflow.utils.MangaClass;
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

public class HomeActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private MangaAdapter adapter;
    private List<MangaClass> mangaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 1. Charger la liste depuis le JSON
        if (isNetworkAvailable()) {
            mangaList = loadMangasFromJSON();

            // 2. Créer l'adapter AVEC la liste remplie
            adapter = new MangaAdapter(mangaList, R.layout.manga_item);

            // 3. Lier l'adapter au RecyclerView
            recyclerView.setAdapter(adapter);

            // 4. Forcer l'affichage
            adapter.notifyDataSetChanged();
        } else {
            // Afficher un message d'erreur si pas de Wi-Fi/4G
            Toast.makeText(this, "Pas de connexion internet", Toast.LENGTH_LONG).show();
        };

        // Liaison des boutons XML vers le code Java
        ImageView btnPlanning = findViewById(R.id.btn_planning);
        ImageView btnCollection = findViewById(R.id.btn_collection);
        ImageView btnSearch = findViewById(R.id.btn_search);
        ImageView btnMaps = findViewById(R.id.btn_maps);
        ImageView btnAccount = findViewById(R.id.btn_account);

        // Configuration des clics

        // Vérification de la connexion
        btnAccount.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            String userEmail = pref.getString("user_email", null);

            if (userEmail != null) {
                // L'utilisateur est connecté -> Page de déconnexion
                // Tu peux créer une nouvelle activité "LogoutActivity"
                Intent intent = new Intent(HomeActivity.this, LogoutActivity.class);
                startActivity(intent);
            } else {
                // L'utilisateur est déconnecté -> Page de Login
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Affichage des dernières sorties
        chargerMangasHome();

        // Vers le Planning
        btnPlanning.setOnClickListener(v -> {
            Intent PlanningIntent = new Intent(HomeActivity.this, PlanningActivity.class);
            startActivity(PlanningIntent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Vers la Collection
        btnCollection.setOnClickListener(v -> {
            Intent CollectionIntent = new Intent(HomeActivity.this, CollectionActivity.class);
            startActivity(CollectionIntent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Vers la Recherche
        btnSearch.setOnClickListener(v -> {
            Intent SearchIntent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(SearchIntent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Vers la Carte
        btnMaps.setOnClickListener(v -> {
            Intent MapsIntent = new Intent(HomeActivity.this, MapsActivity.class);
            startActivity(MapsIntent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private List<MangaClass> loadMangasFromJSON() {
        List<MangaClass> list = new ArrayList<>();
        try {
            // Lecture depuis le dossier 'raw'
            InputStream is = getResources().openRawResource(R.raw.mangas);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            // Parsing du JSON
            JSONArray jsonArray = new JSONArray(json);
            int numTome = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                // Extraction sécurisée
                String titre = obj.optString("titre_serie", "Inconnu");
                int tome = obj.optInt("numero_tome", 0); // optInt gère String ou Int automatiquement
                String img = obj.optString("image_url", "");
                String edit = obj.optString("edition", "");
                String isbn = obj.optString("ean", "0000000000000"); // Ton JSON semble utiliser "ean" au lieu de "isbn"
                String editeur = obj.optString("editeur_fr", "");
                String date = obj.optString("date_parution", "");
                String resume = obj.optString("resume", "");
                Boolean lu = obj.optBoolean("lu", false);
                Boolean suivi = obj.optBoolean("suivi", false);
                Boolean possede = obj.optBoolean("possede", false);

                // On crée l'objet avec les données récupérées
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

    private void chargerMangasHome() {
        try {
            InputStream is = getResources().openRawResource(R.raw.mangas);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONArray array = new JSONArray(new String(buffer, StandardCharsets.UTF_8));
            List<MangaClass> tempListe = new ArrayList<>();

            // Format de date correspondant à ton JSON : dd/MM/yyyy
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            Date aujourdhui = new Date();

            // On parcourt du BAS vers le HAUT (i = length - 1 down to 0)
            for (int i = array.length() - 1; i >= 0; i--) {
                // Arrêt si on a déjà 50 mangas
                if (tempListe.size() >= 50) break;

                JSONObject obj = array.getJSONObject(i);
                String dateSortieStr = obj.optString("date_parution", ""); // Assure-toi que la clé est correcte

                try {
                    Date dateSortie = sdf.parse(dateSortieStr);

                    // On n'ajoute que si la date de sortie est AVANT ou EGALE à aujourd'hui
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
                } catch (ParseException e) {
                    // Si la date est mal formatée, on peut décider de l'ignorer ou de l'afficher
                    Log.e("HOME_DATE", "Format de date invalide pour : " + dateSortieStr);
                }
            }

            // Mise à jour de l'adapter
            mangaList.clear();
            mangaList.addAll(tempListe);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e("HOME_ERROR", "Erreur lors du chargement : " + e.getMessage());
        }
    }
}