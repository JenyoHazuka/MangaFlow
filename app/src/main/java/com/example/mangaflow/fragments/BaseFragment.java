package com.example.mangaflow.fragments;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mangaflow.R;

/**
 * Classe de base pour les activités nécessitant un affichage plein écran.
 * Elle gère la suppression des barres système et force le thème clair.
 */
public class BaseFragment extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. DÉSACTIVATION DU MODE SOMBRE :
        // Force l'application en mode jour pour garantir la lisibilité des couleurs définies en dur.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        // Appel de la méthode pour masquer l'interface système dès le lancement
        hideSystemUI();
    }

    /**
     * Rappel système déclenché quand la fenêtre récupère le focus.
     * Utile pour masquer à nouveau les barres si l'utilisateur a ouvert le volet de notifications.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    /**
     * Utilise WindowInsetsController pour passer en mode immersif.
     */
    private void hideSystemUI() {
        // Récupération du contrôleur de la fenêtre actuelle
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());

        if (windowInsetsController != null) {
            // 2. MASQUAGE DES BARRES :
            // Cache la barre de statut (haut) et la barre de navigation (bas/boutons)
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

            // 3. COMPORTEMENT TEMPORAIRE :
            // Les barres réapparaissent brièvement si on swipe depuis le bord de l'écran,
            // puis se recachent automatiquement après quelques secondes.
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
    }
}