package com.example.mangaflow.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mangaflow.activities.MangaActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.activities.CollectionActivity;
import com.example.mangaflow.utils.SerieAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment principal de la bibliothèque utilisateur.
 * Gère l'affichage des séries possédées, le calcul de progression et le scan ISBN.
 */
public class CollectionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        // 1. CONFIGURATION DU SCAN : Bouton déclenchant l'appareil photo
        view.findViewById(R.id.btn_scan).setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.EAN_13); // Format ISBN standard
            options.setPrompt("Scannez le code-barre du manga");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            barcodeLauncher.launch(options);
        });

        CollectionActivity activity = (CollectionActivity) getActivity();
        if (activity == null) return view;

        // Récupération des données chargées par l'activité parente
        JSONArray collection = activity.getCollection(); // Données privées de l'utilisateur
        JSONArray seriesRef = activity.getSeriesReference(); // Référentiel global (raw/series.json)

        TextView tvTomes = view.findViewById(R.id.tv_total_tomes);
        TextView tvSeries = view.findViewById(R.id.tv_total_series);
        RecyclerView rv = view.findViewById(R.id.rv_collection);

        int countTotalTomes = 0;
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            // 2. LOGIQUE DE SYNTHÈSE : On croise les données utilisateur avec le référentiel
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                String nomSerie = serie.optString("nom");

                // Étape A : On cherche le nombre total théorique de tomes dans series.json
                int totalTheorique = 0;
                String statut = "";
                for (int j = 0; j < seriesRef.length(); j++) {
                    JSONObject ref = seriesRef.getJSONObject(j);
                    if (ref.optString("titre").equalsIgnoreCase(nomSerie)) {
                        JSONArray editions = ref.optJSONArray("editions");
                        if (editions != null && editions.length() > 0) {
                            JSONObject firstEd = editions.getJSONObject(0);
                            String rawNb = firstEd.optString("nb_tomes", "0");
                            // Nettoyage de la chaîne (ex: "34 tomes" -> 34)
                            totalTheorique = Integer.parseInt(rawNb.replaceAll("[^0-9]", ""));
                            statut = firstEd.optString("statut", "");
                        }
                        break;
                    }
                }

                // Étape B : On compte combien de tomes l'utilisateur possède réellement
                JSONArray mangas = serie.optJSONArray("mangas");
                int nbPossedes = 0;
                if (mangas != null) {
                    for (int k = 0; k < mangas.length(); k++) {
                        if (mangas.getJSONObject(k).optBoolean("posséder", false)) {
                            nbPossedes++;
                            countTotalTomes++;
                        }
                    }
                }

                // Étape C : Préparation de l'objet pour le SerieAdapter
                if (nbPossedes > 0) {
                    serie.put("nb_possedes", nbPossedes);
                    serie.put("nombre_tome_total", totalTheorique);
                    serie.put("statut", statut);
                    serie.put("affichage_collection", true); // Flag pour afficher la ProgressBar
                    filteredList.add(serie);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Mise à jour des compteurs en haut de page
        tvTomes.setText(countTotalTomes + " Tomes");
        tvSeries.setText(filteredList.size() + " Séries");

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }

    // --- GESTION DU SCANNER (Zxing / JourneyApps) ---

    // Callback recevant le résultat du scan (le code EAN-13)
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    rechercherMangaParISBN(result.getContents());
                }
            });

    /**
     * Recherche le code scanné dans la base mangas.json
     */
    private void rechercherMangaParISBN(String isbn) {
        try {
            InputStream is = getResources().openRawResource(R.raw.mangas);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONArray allMangas = new JSONArray(new String(buffer, StandardCharsets.UTF_8));

            JSONObject mangaTrouve = null;
            for (int i = 0; i < allMangas.length(); i++) {
                // Comparaison de l'EAN scanné avec celui du JSON
                if (allMangas.getJSONObject(i).optString("ean").equals(isbn)) {
                    mangaTrouve = allMangas.getJSONObject(i);
                    break;
                }
            }

            if (mangaTrouve != null) {
                // Si trouvé : Ajout silencieux en base et redirection vers la fiche
                ajouterMangaALaCollectionAutomatique(mangaTrouve);

                Intent intent = new Intent(getContext(), MangaActivity.class);
                intent.putExtra("TITRE_MANGA", mangaTrouve.optString("titre_serie"));
                intent.putExtra("NUMERO_TOME", mangaTrouve.optInt("numero_tome"));
                startActivity(intent);

                Toast.makeText(getContext(), "Manga ajouté !", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Code ISBN inconnu", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) { Log.e("SCAN_AUTO", "Erreur : " + e.getMessage()); }
    }

    /**
     * Inscrit le manga scanné dans le fichier JSON privé de l'utilisateur.
     */
    private void ajouterMangaALaCollectionAutomatique(JSONObject mangaObj) {
        try {
            SharedPreferences pref = requireContext().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE);
            String email = pref.getString("user_email", null);
            if (email == null) return;

            File file = new File(requireContext().getFilesDir(), email + "_data.json");
            JSONObject userData = file.exists() ? new JSONObject(loadStringFromFile(file)) : new JSONObject();
            if (!userData.has("collection")) userData.put("collection", new JSONArray());
            JSONArray collection = userData.getJSONArray("collection");

            String titreSerie = mangaObj.optString("titre_serie");
            int numTome = mangaObj.optInt("numero_tome");

            // Recherche de la série existante ou création
            JSONObject serieJson = null;
            for (int i = 0; i < collection.length(); i++) {
                if (collection.getJSONObject(i).getString("nom").equalsIgnoreCase(titreSerie)) {
                    serieJson = collection.getJSONObject(i);
                    break;
                }
            }
            if (serieJson == null) {
                serieJson = new JSONObject();
                serieJson.put("nom", titreSerie);
                serieJson.put("mangas", new JSONArray());
                collection.put(serieJson);
            }

            JSONArray mangasArray = serieJson.getJSONArray("mangas");

            // Recherche du tome ou création
            JSONObject tomeJson = null;
            for (int i = 0; i < mangasArray.length(); i++) {
                if (mangasArray.getJSONObject(i).optInt("numéro") == numTome) {
                    tomeJson = mangasArray.getJSONObject(i);
                    break;
                }
            }
            if (tomeJson == null) {
                tomeJson = new JSONObject();
                tomeJson.put("numéro", numTome);
                mangasArray.put(tomeJson);
            }

            // Mise à jour des états (Possédé par défaut après un scan)
            tomeJson.put("posséder", true);
            tomeJson.put("souhaiter", true);
            tomeJson.put("jaquette", mangaObj.optString("image_url"));

            saveStringToFile(file, userData.toString());

        } catch (Exception e) { Log.e("SAVE_AUTO", "Erreur : " + e.getMessage()); }
    }

    // Méthodes utilitaires de lecture/écriture de fichiers
    private String loadStringFromFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data); fis.close();
        return new String(data, StandardCharsets.UTF_8);
    }

    private void saveStringToFile(File file, String content) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }
}