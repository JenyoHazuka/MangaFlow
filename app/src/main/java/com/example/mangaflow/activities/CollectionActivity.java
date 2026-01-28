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

/**
 * Activité principale de la bibliothèque de l'utilisateur.
 * Gère l'affichage de la collection, des listes de souhaits et des mangas à compléter via des onglets.
 */
public class CollectionActivity extends BaseActivity {

    // Déclaration des boutons de filtrage (onglets)
    private Button btnColl, btnComp, btnSouh, btnLire;
    // Stockage des données de l'utilisateur chargé depuis son fichier JSON local
    private JSONArray userCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. SÉCURITÉ : Vérification de la session via SharedPreferences
        // On récupère l'email stocké lors du login pour savoir quel fichier charger
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userEmail = pref.getString("user_email", null);

        if (userEmail == null) {
            // Si aucune session n'est trouvée, redirection immédiate vers l'écran de connexion
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // On détruit cette activité pour empêcher le retour arrière
            return;
        }

        // 2. INTERFACE : Chargement du layout (uniquement si l'utilisateur est authentifié)
        setContentView(R.layout.activity_collection);

        // 3. DONNÉES : Chargement des données privées de l'utilisateur (mangas possédés, etc.)
        loadUserCollection(userEmail);

        // 4. INITIALISATION : Liaison des composants du layout avec le code Java
        btnColl = findViewById(R.id.btn_collection);
        btnComp = findViewById(R.id.btn_completer);
        btnSouh = findViewById(R.id.btn_souhaiter);
        btnLire = findViewById(R.id.btn_a_lire);
        ImageView ivAccount = findViewById(R.id.iv_account_light);

        // 5. LANCEMENT : Affichage du premier fragment (la collection complète) au démarrage
        if (savedInstanceState == null) {
            replaceFragment(new CollectionFragment());
            updateButtonUI(btnColl); // Mise en couleur du bouton "Collection"
        }

        // 6. NAVIGATION INTERNE : Changement de fragment au clic sur les onglets du haut
        btnColl.setOnClickListener(v -> { replaceFragment(new CollectionFragment()); updateButtonUI(btnColl); });
        btnComp.setOnClickListener(v -> { replaceFragment(new CompleterFragment()); updateButtonUI(btnComp); });
        btnSouh.setOnClickListener(v -> { replaceFragment(new SouhaiterFragment()); updateButtonUI(btnSouh); });
        btnLire.setOnClickListener(v -> { replaceFragment(new ALireFragment()); updateButtonUI(btnLire); });

        // 7. COMPTE : Redirection vers les paramètres du compte / déconnexion
        ivAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, LogoutActivity.class);
            startActivity(intent);
        });

        // --- NAVIGATION INFÉRIEURE (Barre de menu globale) ---
        findViewById(R.id.btn_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });

        findViewById(R.id.btn_collection).setOnClickListener(v -> {
            // Déjà sur cette activité, mais permet de rafraîchir ou revenir au début
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

    /**
     * Getter public pour permettre aux Fragments (CollectionFragment, etc.)
     * de récupérer la liste des mangas sans recharger le fichier.
     */
    public JSONArray getCollection() {
        return (userCollection != null) ? userCollection : new JSONArray();
    }

    /**
     * Lit le fichier JSON stocké en mémoire interne du téléphone (Internal Storage).
     * Le fichier est nommé d'après l'email : "email_data.json".
     */
    private void loadUserCollection(String email) {
        try {
            File file = new File(getFilesDir(), email + "_data.json");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                String json = new String(buffer, StandardCharsets.UTF_8);

                JSONObject userObj = new JSONObject(json);
                // On extrait uniquement le tableau "collection" du JSON utilisateur
                userCollection = userObj.getJSONArray("collection");
            } else {
                // Si l'utilisateur n'a pas encore de fichier (première connexion)
                userCollection = new JSONArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
            userCollection = new JSONArray();
        }
    }

    /**
     * Gère le remplacement dynamique de la vue centrale par un Fragment.
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Met à jour l'apparence visuelle des boutons du haut pour indiquer l'onglet actif.
     * @param activeBtn Le bouton sur lequel l'utilisateur vient de cliquer.
     */
    private void updateButtonUI(Button activeBtn) {
        Button[] buttons = {btnColl, btnComp, btnSouh, btnLire};
        for (Button b : buttons) {
            if (b == activeBtn) {
                // État actif : Couleur de la charte graphique et texte blanc
                b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#007FFF")));
                b.setTextColor(Color.WHITE);
            } else {
                // État inactif : Teinte grise neutre
                b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
                b.setTextColor(Color.BLACK);
            }
        }
    }

    /**
     * Charge le catalogue de référence (toutes les séries existantes) depuis les ressources.
     * Utile pour certains fragments qui comparent la collection avec la base complète.
     */
    public JSONArray getSeriesReference() {
        try {
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