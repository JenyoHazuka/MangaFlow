package com.example.mangaflow.utils;

import android.content.Context;

import com.example.mangaflow.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;

public class SecurityUtils {
        // Chiffrement SHA-256
        private static final String FILE_NAME = "users.json";

    // Hachage SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) { return null; }
    }

    // Lire les utilisateurs (Raw puis Interne)
    public static JSONArray getUsers(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        try {
            String content;
            if (file.exists()) {
                content = new String(Files.readAllBytes(file.toPath()));
            } else {
                // Lecture initiale depuis res/raw/mangas.json ou users.json
                InputStream is = context.getResources().openRawResource(R.raw.mangas); // Adapte le nom
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                content = new String(buffer);
            }
            return new JSONArray(content);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static void saveUsers(Context context, JSONArray users) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            Files.write(file.toPath(), users.toString().getBytes());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static JSONObject validateLogin(Context context, String email, String password) {
        JSONArray users = getUsers(context); // Récupère la liste (Raw)
        String hashedInput = hashPassword(password); // Chiffre le mot de passe saisi

        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                // On compare l'email et le hash du mot de passe
                if (user.getString("email").equals(email) &&
                        user.getString("password").equals(hashedInput)) {
                    return user; // Retourne l'utilisateur si tout est bon
                }
            } catch (JSONException e) { e.printStackTrace(); }
        }
        return null; // Identifiants incorrects
    }
}
