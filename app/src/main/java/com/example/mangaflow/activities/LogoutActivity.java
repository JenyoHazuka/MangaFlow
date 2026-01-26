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

public class LogoutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Liaison des boutons XML vers le code Java
        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnCancel = findViewById(R.id.btn_cancel);

        // Gestion des clics
        btnLogout.setOnClickListener(v -> {
            // 1. Effacer la session
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            pref.edit().clear().apply();

            // 2. Retourner à l'accueil ou au Login
            Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LogoutActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Le bouton "NON" fait simplement un finish() pour revenir en arrière
        btnCancel.setOnClickListener(v -> finish());
    }
}