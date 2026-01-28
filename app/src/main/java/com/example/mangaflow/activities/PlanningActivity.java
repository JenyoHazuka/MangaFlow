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

public class PlanningActivity extends BaseActivity {

    private Button btnTout, btnNouveautes;
    private int colorActive = Color.parseColor("#0083ff");
    private int colorInactive = Color.parseColor("#E0E0E0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        // Récupération des deux boutons restants
        btnTout = findViewById(R.id.btn_filter_tout);
        btnNouveautes = findViewById(R.id.btn_filter_nouveautes);

        // Par défaut, on affiche l'onglet "Tout" au démarrage
        switchFragment(new PlanningToutFragment(), btnTout);

        // --- LISTENERS ---
        btnTout.setOnClickListener(v -> switchFragment(new PlanningToutFragment(), btnTout));
        btnNouveautes.setOnClickListener(v -> switchFragment(new PlanningNouveautesFragment(), btnNouveautes));

        // --- NAVIGATION ---
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, CollectionActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.btn_maps).setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
    }

    private void switchFragment(Fragment fragment, Button activeBtn) {
        // Reset des couleurs pour les deux boutons visibles
        btnTout.setBackgroundTintList(ColorStateList.valueOf(colorInactive));
        btnTout.setTextColor(Color.parseColor("#333333"));
        btnNouveautes.setBackgroundTintList(ColorStateList.valueOf(colorInactive));
        btnNouveautes.setTextColor(Color.parseColor("#333333"));

        // Activer le bouton sélectionné
        activeBtn.setBackgroundTintList(ColorStateList.valueOf(colorActive));
        activeBtn.setTextColor(Color.WHITE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_planning, fragment)
                .commit();
    }

    public JSONArray getMangasData() {
        try {
            InputStream is = getResources().openRawResource(R.raw.mangas);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new JSONArray(new String(buffer, "UTF-8"));
        } catch (Exception e) {
            Log.e("Planning", "Erreur JSON", e);
            return new JSONArray();
        }
    }

    public boolean isFutureOrToday(String dateString) {
        if (dateString == null || dateString.isEmpty()) return false;
        try {
            // Format dd/MM/yyyy
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE);
            java.util.Date dateManga = sdf.parse(dateString);

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            java.util.Date aujourdhui = cal.getTime();

            return dateManga != null && !dateManga.before(aujourdhui);
        } catch (Exception e) {
            return false;
        }
    }
}