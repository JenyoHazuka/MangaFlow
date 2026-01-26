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


public class RegisterActivity extends BaseActivity {

    private EditText editEmail, editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Liaison avec l'ID du XML
        ImageView btnBack = findViewById(R.id.btn_back);

        // Gestion du clic
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Cette commande ferme l'activité actuelle et revient à la précédente
                finish();

                // ajouter une animation de sortie (glissement vers la droite)
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            });
        }

        TextView tvLogIn = findViewById(R.id.tv_log_in);

        if (tvLogIn != null) {
            tvLogIn.setOnClickListener(v -> {
                // Redirection vers LoginActivity
                Intent LoginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(LoginIntent);

                // animation de transition
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Inscription
        editEmail = findViewById(R.id.email_register);
        editPassword = findViewById(R.id.password_register);

        findViewById(R.id.btn_register).setOnClickListener(v -> {
            registerUser();
        });
    }

    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Validation du format du mot de passe (Regex)
        // Majuscule, minuscule, spécial, min 8 car.
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!.?,$])(?=\\S+$).{8,}$";

        if (!password.matches(passwordPattern)) {
            Toast.makeText(this, "Le mot de passe doit contenir 8 caractères, une majuscule, une minuscule et un caractère spécial", Toast.LENGTH_LONG).show();
            return;
        }

        // Charger les utilisateurs existants
        JSONArray users = SecurityUtils.getUsers(this);

        // Vérifier si l'email existe déjà
        for (int i = 0; i < users.length(); i++) {
            try {
                if (users.getJSONObject(i).getString("email").equals(email)) {
                    Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (JSONException e) { e.printStackTrace(); }
        }

        // Créer le nouvel utilisateur
        try {
            JSONObject newUser = new JSONObject();
            newUser.put("email", email);
            newUser.put("password", SecurityUtils.hashPassword(password));
            newUser.put("collection", new JSONArray()); // Liste vide au départ

            users.put(newUser);
            SecurityUtils.saveUsers(this, users);

            Toast.makeText(this, "Inscription validée !", Toast.LENGTH_SHORT).show();
            finish(); // Retour au Login
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}