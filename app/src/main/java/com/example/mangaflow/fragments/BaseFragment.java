package com.example.mangaflow.fragments;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mangaflow.R;

public class BaseFragment extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force le mode jour pour éviter les bugs de couleurs
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        hideSystemUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());

        if (windowInsetsController != null) {
            // Masque la barre de statut (heure) et la barre de navigation (boutons)
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

            // Permet l'affichage temporaire au swipe
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
    }
}