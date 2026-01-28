package com.example.mangaflow.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mangaflow.R;
import com.example.mangaflow.utils.SecurityUtils;

import org.json.JSONObject;

/**
 * Activité gérant la connexion des utilisateurs.
 * Elle vérifie les identifiants et initialise la session locale.
 */
public class LoginActivity extends BaseActivity {

    // Champs de saisie pour l'email et le mot de passe
    private EditText editEmail, editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activation du mode Edge-to-Edge pour que l'interface utilise toute la surface de l'écran
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Gestion des marges (Padding) pour éviter que l'UI ne soit cachée par les barres système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. BOUTON RETOUR : Liaison et gestion du clic
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Ferme la page de login pour revenir à l'écran précédent (ex: Home ou Splash)
                finish();
                // Transition : glissement vers la droite
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            });
        }

        // 2. LIEN INSCRIPTION : Redirection vers la page de création de compte
        TextView tvSignUp = findViewById(R.id.tv_sign_up);
        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                Intent RegisterIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(RegisterIntent);
                // Transition : fondu enchaîné
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // 3. INITIALISATION DES CHAMPS DE SAISIE
        editEmail = findViewById(R.id.email_input);
        editPassword = findViewById(R.id.password_input);

        // 4. ACTION DE CONNEXION : Déclenche la validation au clic sur le bouton
        findViewById(R.id.btn_login).setOnClickListener(v -> {
            performLogin();
        });
    }

    /**
     * Logique de validation des identifiants et ouverture de session.
     */
    private void performLogin() {
        // Récupération des textes saisis et suppression des espaces inutiles
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Sécurité : Vérifier que les champs ne sont pas vides
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        /* VÉRIFICATION DES IDENTIFIANTS :
           On utilise SecurityUtils pour comparer l'email et le mot de passe avec
           le fichier JSON de stockage des utilisateurs.
        */
        JSONObject user = SecurityUtils.validateLogin(this, email, password);

        if (user != null) {
            /* CONNEXION RÉUSSIE :
               1. Ouverture d'un espace de stockage SharedPreferences appelé "UserSession"
               2. Sauvegarde de l'email pour maintenir la connexion au prochain lancement
            */
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("user_email", email);
            editor.apply(); // Enregistrement asynchrone

            // Redirection vers l'accueil
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);

            // On ferme le LoginActivity pour que l'utilisateur ne puisse pas revenir au login via le bouton retour
            finish();
        } else {
            // ÉCHEC : Message d'erreur si le couple email/password ne correspond pas
            Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}