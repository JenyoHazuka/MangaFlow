package com.example.mangaflow.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mangaflow.R;

/**
 * Activité de confirmation de déconnexion.
 * Permet de supprimer les données de session et de réinitialiser la pile d'activités.
 */
public class LogoutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activation du mode bord à bord (EdgeToEdge) pour une immersion visuelle totale
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logout);

        // Ajustement automatique des marges pour ne pas empiéter sur les barres système (statut/navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. INITIALISATION : Liaison des boutons du fichier XML activity_logout.xml
        Button btnLogout = findViewById(R.id.btn_logout); // Le bouton "OUI"
        Button btnCancel = findViewById(R.id.btn_cancel); // Le bouton "NON"

        // 2. LOGIQUE DE DÉCONNEXION (Bouton OUI)
        btnLogout.setOnClickListener(v -> {
            // A. ACCÈS AUX PRÉFÉRENCES : On cible le fichier "UserSession" créé lors du Login
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);

            /* B. NETTOYAGE :
               .clear() supprime toutes les clés (user_email, etc.)
               .apply() enregistre les modifications immédiatement en arrière-plan
            */
            pref.edit().clear().apply();

            // C. FEEDBACK : Petite notification pour confirmer l'action à l'utilisateur
            Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();

            // D. REDIRECTION ET SÉCURITÉ :
            Intent intent = new Intent(LogoutActivity.this, HomeActivity.class);

            /* FLAGS DE NAVIGATION :
               FLAG_ACTIVITY_NEW_TASK et FLAG_ACTIVITY_CLEAR_TASK permettent de vider
               tout l'historique des pages précédentes.
               L'utilisateur ne pourra pas faire "Retour" pour revenir dans sa session fermée.
            */
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            // On ferme définitivement cette page
            finish();
        });

        // 3. ANNULATION (Bouton NON)
        /* On utilise simplement finish() : l'activité se ferme et l'utilisateur
           retombe naturellement sur la page où il était (probablement Home ou Collection).
        */
        btnCancel.setOnClickListener(v -> finish());
    }
}