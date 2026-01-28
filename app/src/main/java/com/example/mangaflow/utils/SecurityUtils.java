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

/**
 * Utilitaire gérant la sécurité (hachage) et la persistance des données utilisateurs.
 */
public class SecurityUtils {

    // Nom du fichier JSON stocké dans le stockage interne de l'application
    private static final String FILE_NAME = "users.json";

    /**
     * Transforme un mot de passe en clair en empreinte numérique unique (Hash).
     * Utilise l'algorithme SHA-256 : une modification d'un seul caractère
     * change totalement le résultat.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            // Conversion du tableau de bytes en format Hexadécimal (chaîne de caractères)
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Récupère la liste des utilisateurs.
     * Priorité au fichier modifiable en stockage interne, sinon lit le fichier par défaut (Raw).
     */
    public static JSONArray getUsers(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        try {
            String content;
            if (file.exists()) {
                // Lecture depuis le stockage privé (/data/user/0/...)
                content = new String(Files.readAllBytes(file.toPath()));
            } else {
                // Premier lancement : lecture de la ressource brute (statique)
                InputStream is = context.getResources().openRawResource(R.raw.mangas); // Note : Vérifie si c'est R.raw.users ici
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                content = new String(buffer);
            }
            return new JSONArray(content);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    /**
     * Enregistre la liste mise à jour des utilisateurs dans le stockage interne.
     */
    public static void saveUsers(Context context, JSONArray users) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            Files.write(file.toPath(), users.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si le couple Email/Mot de passe correspond à un utilisateur enregistré.
     * @return L'objet utilisateur si valide, null sinon.
     */
    public static JSONObject validateLogin(Context context, String email, String password) {
        JSONArray users = getUsers(context);
        String hashedInput = hashPassword(password); // On hache la saisie pour comparer des hashs entre eux

        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                // Comparaison sécurisée : on ne stocke jamais les mots de passe en clair
                if (user.getString("email").equals(email) &&
                        user.getString("password").equals(hashedInput)) {
                    return user;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}