package com.example.mangaflow.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.example.mangaflow.R;
import com.example.mangaflow.fragments.PlanningToutFragment;
import com.example.mangaflow.fragments.PlanningNouveautesFragment;
import org.json.JSONArray;
import java.io.InputStream;

/**
 * Activité gérant le planning des sorties de mangas.
 * Elle permet de basculer entre la liste complète des sorties futures et les nouveautés.
 */
public class PlanningActivity extends BaseActivity {

    // Déclaration des boutons de filtrage (onglets)
    private Button btnTout, btnNouveautes;

    // Définition des couleurs pour l'état actif (bleu) et inactif (gris)
    private int colorActive = Color.parseColor("#0083ff");
    private int colorInactive = Color.parseColor("#E0E0E0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        // 1. Initialisation des composants UI
        btnTout = findViewById(R.id.btn_filter_tout);
        btnNouveautes = findViewById(R.id.btn_filter_nouveautes);

        // 2. État initial : chargement de l'onglet "Tout" au démarrage de l'activité
        switchFragment(new PlanningToutFragment(), btnTout);

        // 3. LISTENERS : Gestion du clic sur les onglets du haut pour changer de vue
        btnTout.setOnClickListener(v -> switchFragment(new PlanningToutFragment(), btnTout));
        btnNouveautes.setOnClickListener(v -> switchFragment(new PlanningNouveautesFragment(), btnNouveautes));

        // 4. NAVIGATION : Configuration des boutons de la barre de navigation inférieure
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
    }

    /**
     * Gère le remplacement du fragment affiché et la mise à jour visuelle des boutons.
     * @param fragment Le nouveau fragment à afficher.
     * @param activeBtn Le bouton sur lequel l'utilisateur a cliqué.
     */
    private void switchFragment(Fragment fragment, Button activeBtn) {
        // Réinitialisation de l'apparence des deux boutons (état inactif par défaut)
        btnTout.setBackgroundTintList(ColorStateList.valueOf(colorInactive));
        btnTout.setTextColor(Color.parseColor("#333333"));
        btnNouveautes.setBackgroundTintList(ColorStateList.valueOf(colorInactive));
        btnNouveautes.setTextColor(Color.parseColor("#333333"));

        // Mise en avant du bouton sélectionné (état actif)
        activeBtn.setBackgroundTintList(ColorStateList.valueOf(colorActive));
        activeBtn.setTextColor(Color.WHITE);

        // Exécution de la transaction pour remplacer le fragment dans le conteneur XML
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_planning, fragment)
                .commit();
    }

    /**
     * Charge la base de données brute des mangas depuis les ressources de l'application.
     * @return JSONArray contenant tous les mangas du fichier raw/mangas.json.
     */
    public JSONArray getMangasData() {
        try {
            InputStream is = getResources().openRawResource(R.raw.mangas);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new JSONArray(new String(buffer, "UTF-8"));
        } catch (Exception e) {
            Log.e("Planning", "Erreur lors de la lecture du JSON", e);
            return new JSONArray();
        }
    }

    /**
     * Utilitaire de comparaison de dates pour filtrer les sorties.
     * @param dateString La date du manga au format "dd/MM/yyyy".
     * @return true si la date est aujourd'hui ou dans le futur, false sinon.
     */
    public boolean isFutureOrToday(String dateString) {
        if (dateString == null || dateString.isEmpty()) return false;
        try {
            // Définition du format de date utilisé dans le JSON
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE);
            java.util.Date dateManga = sdf.parse(dateString);

            // Création d'un calendrier calé sur aujourd'hui à minuit pour la comparaison
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            java.util.Date aujourdhui = cal.getTime();

            // Retourne vrai si la date n'est pas passée
            return dateManga != null && !dateManga.before(aujourdhui);
        } catch (Exception e) {
            return false;
        }
    }
}