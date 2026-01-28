package com.example.mangaflow.activities;

import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activité gérant la création d'un nouveau compte utilisateur.
 * Inclut des vérifications de sécurité et le stockage dans le JSON des utilisateurs.
 */
public class RegisterActivity extends BaseActivity {

    private EditText editEmail, editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activation du mode plein écran (Edge-to-Edge)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Gestion des marges pour les barres système (statut et navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. BOUTON RETOUR : Fermeture de l'activité avec animation
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            });
        }

        // 2. LIEN VERS CONNEXION : Redirection si l'utilisateur a déjà un compte
        TextView tvLogIn = findViewById(R.id.tv_log_in);
        if (tvLogIn != null) {
            tvLogIn.setOnClickListener(v -> {
                Intent LoginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(LoginIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // 3. INITIALISATION DES CHAMPS DE SAISIE
        editEmail = findViewById(R.id.email_register);
        editPassword = findViewById(R.id.password_register);

        // 4. ACTION D'INSCRIPTION : Déclenchement de la logique de création
        findViewById(R.id.btn_register).setOnClickListener(v -> {
            registerUser();
        });
    }

    /**
     * Valide les données saisies et enregistre le nouvel utilisateur.
     */
    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        /* VALIDATION DU MOT DE PASSE :
           Utilisation d'un Regex pour exiger :
           - Au moins un chiffre [0-9]
           - Une minuscule [a-z] et une majuscule [A-Z]
           - Un caractère spécial [@#$%^&+=!.?,$]
           - Minimum 8 caractères
        */
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!.?,$])(?=\\S+$).{8,}$";

        if (!password.matches(passwordPattern)) {
            Toast.makeText(this, "Le mot de passe doit contenir 8 caractères, une majuscule, une minuscule et un caractère spécial", Toast.LENGTH_LONG).show();
            return;
        }

        // CHARGEMENT : Récupération de la liste actuelle des utilisateurs via SecurityUtils
        JSONArray users = SecurityUtils.getUsers(this);

        // VÉRIFICATION D'UNICITÉ : On vérifie que l'email n'est pas déjà enregistré
        for (int i = 0; i < users.length(); i++) {
            try {
                if (users.getJSONObject(i).getString("email").equals(email)) {
                    Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (JSONException e) { e.printStackTrace(); }
        }

        // CRÉATION DU COMPTE
        try {
            JSONObject newUser = new JSONObject();
            newUser.put("email", email);

            /* SÉCURITÉ : Le mot de passe n'est jamais enregistré en clair.
               On enregistre une version hachée via SecurityUtils.hashPassword.
            */
            newUser.put("password", SecurityUtils.hashPassword(password));
            newUser.put("collection", new JSONArray()); // Initialise une bibliothèque vide

            // AJOUT ET SAUVEGARDE : Mise à jour du fichier JSON global des utilisateurs
            users.put(newUser);
            SecurityUtils.saveUsers(this, users);

            Toast.makeText(this, "Inscription validée !", Toast.LENGTH_SHORT).show();

            // Fin de l'activité pour retourner automatiquement à l'écran de Login
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}