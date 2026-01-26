package com.example.mangaflow.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.mangaflow.R;
import com.example.mangaflow.fragments.ALireFragment;
import com.example.mangaflow.fragments.CollectionFragment;
import com.example.mangaflow.fragments.CompleterFragment;
import com.example.mangaflow.fragments.SouhaiterFragment;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CollectionActivity extends BaseActivity {

    private Button btnColl, btnComp, btnSouh, btnLire;
    private JSONArray userCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. SÉCURITÉ : Vérification de la session AVANT tout chargement
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userEmail = pref.getString("user_email", null);

        if (userEmail == null) {
            // Pas de session -> Redirection Login et on ferme l'activité
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 2. INTERFACE : Chargement du layout si l'utilisateur est connecté
        setContentView(R.layout.activity_collection);

        // 3. DONNÉES : Chargement de la collection depuis le fichier JSON
        loadUserCollection(userEmail);

        // 4. INITIALISATION : Liaison des composants UI
        btnColl = findViewById(R.id.btn_collection);
        btnComp = findViewById(R.id.btn_completer);
        btnSouh = findViewById(R.id.btn_souhaiter);
        btnLire = findViewById(R.id.btn_a_lire);
        ImageView ivAccount = findViewById(R.id.iv_account_light);

        // 5. LANCEMENT : Chargement du Fragment par défaut (Collection)
        if (savedInstanceState == null) {
            replaceFragment(new CollectionFragment());
            updateButtonUI(btnColl);
        }

        // 6. NAVIGATION : Listeners pour les onglets
        btnColl.setOnClickListener(v -> { replaceFragment(new CollectionFragment()); updateButtonUI(btnColl); });
        btnComp.setOnClickListener(v -> { replaceFragment(new CompleterFragment()); updateButtonUI(btnComp); });
        btnSouh.setOnClickListener(v -> { replaceFragment(new SouhaiterFragment()); updateButtonUI(btnSouh); });
        btnLire.setOnClickListener(v -> { replaceFragment(new ALireFragment()); updateButtonUI(btnLire); });

        // 7. COMPTE : Gestion du clic sur l'icône de profil
        ivAccount.setOnClickListener(v -> {
            // Puisque nous avons vérifié la session au début, userEmail est forcément non-nul ici
            Intent intent = new Intent(this, LogoutActivity.class);
            startActivity(intent);
        });

        // --- NAVIGATION ---
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
    }

    // Utilisé par les Fragments pour obtenir les données
    public JSONArray getCollection() {
        return (userCollection != null) ? userCollection : new JSONArray();
    }

    private void loadUserCollection(String email) {
        try {
            // Lecture du fichier JSON spécifique à l'utilisateur
            File file = new File(getFilesDir(), email + "_data.json");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                String json = new String(buffer, StandardCharsets.UTF_8);

                JSONObject userObj = new JSONObject(json);
                userCollection = userObj.getJSONArray("collection");
            } else {
                // Initialise un tableau vide pour les nouveaux inscrits
                userCollection = new JSONArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
            userCollection = new JSONArray();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void updateButtonUI(Button activeBtn) {
        Button[] buttons = {btnColl, btnComp, btnSouh, btnLire};
        for (Button b : buttons) {
            if (b == activeBtn) {
                // État actif : Bleu avec texte blanc
                b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#007FFF")));
                b.setTextColor(Color.WHITE);
            } else {
                // État inactif : Gris avec texte noir
                b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
                b.setTextColor(Color.BLACK);
            }
        }
    }

    public JSONArray getSeriesReference() {
        try {
            // Lecture du fichier depuis res/raw/series.json
            InputStream is = getResources().openRawResource(R.raw.series);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new JSONArray(new String(buffer, "UTF-8"));
        } catch (Exception e) {
            Log.e("JSON_ERROR", "Erreur lecture series.JSON", e);
            return new JSONArray();
        }
    }
}