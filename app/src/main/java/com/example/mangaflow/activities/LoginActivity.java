package com.example.mangaflow.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mangaflow.R;
import com.example.mangaflow.utils.SecurityUtils;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
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

        TextView tvSignUp = findViewById(R.id.tv_sign_up);

        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                // Redirection vers RegisterActivity
                Intent RegisterIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(RegisterIntent);

                // animation de transition
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        editEmail = findViewById(R.id.email_input);
        editPassword = findViewById(R.id.password_input);

        findViewById(R.id.btn_login).setOnClickListener(v -> {
            performLogin();
        });
    }

    private void performLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérification via FileUtils
        JSONObject user = SecurityUtils.validateLogin(this, email, password);

        if (user != null) {
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("user_email", email);
            editor.apply(); // On enregistre la session

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}