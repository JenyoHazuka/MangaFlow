package com.example.mangaflow.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Classe parente de toutes les activités de l'application.
 * Elle centralise la configuration visuelle commune (Plein écran, Mode jour forcé).
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force l'application à rester en mode jour (désactive le mode sombre automatique)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
    }

    /**
     * Cette méthode est appelée chaque fois que la fenêtre de l'activité gagne ou perd le focus.
     * On s'en sert pour réactiver le mode plein écran quand l'utilisateur revient sur l'application.
     * @param hasFocus Vrai si l'utilisateur est sur l'activité, faux sinon.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Si l'activité redevient active, on cache à nouveau les barres système
            hideSystemUI();
        }
    }

    /**
     * Configure le mode "Immersif" pour masquer les barres de statut (batterie, heure)
     * et la barre de navigation (boutons retour/home d'Android).
     */
    private void hideSystemUI() {
        // Récupération du contrôleur des insets de la fenêtre (gestion des barres système)
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());

        if (windowInsetsController == null) return;

        /* Définit le comportement des barres système :
           BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE permet aux barres de réapparaître
           brièvement si l'utilisateur swipe depuis le bord de l'écran,
           puis de se recacher automatiquement après quelques secondes.
        */
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        // Commande effective pour masquer toutes les barres système (Statut + Navigation)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }
}