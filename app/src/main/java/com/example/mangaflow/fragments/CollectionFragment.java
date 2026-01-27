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

public class CollectionFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        // Configuration du bouton SCAN
        view.findViewById(R.id.btn_scan).setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.EAN_13); // Format standard ISBN
            options.setPrompt("Scannez le code-barre du manga");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            barcodeLauncher.launch(options);
        });

        CollectionActivity activity = (CollectionActivity) getActivity();
        if (activity == null) return view;

        JSONArray collection = activity.getCollection();
        JSONArray seriesRef = activity.getSeriesReference(); // Lit res/raw/series.json

        TextView tvTomes = view.findViewById(R.id.tv_total_tomes);
        TextView tvSeries = view.findViewById(R.id.tv_total_series);
        RecyclerView rv = view.findViewById(R.id.rv_collection);

        int countTotalTomes = 0;
        List<JSONObject> filteredList = new ArrayList<>();

        try {
            for (int i = 0; i < collection.length(); i++) {
                JSONObject serie = collection.getJSONObject(i);
                String nomSerie = serie.optString("nom");

                // 1. Jointure avec series.JSON pour Total et Statut
                int totalTheorique = 0;
                String statut = "";
                for (int j = 0; j < seriesRef.length(); j++) {
                    JSONObject ref = seriesRef.getJSONObject(j);
                    if (ref.optString("titre").equalsIgnoreCase(nomSerie)) {
                        JSONArray editions = ref.optJSONArray("editions");
                        if (editions != null && editions.length() > 0) {
                            JSONObject firstEd = editions.getJSONObject(0);
                            String rawNb = firstEd.optString("nb_tomes", "0");
                            totalTheorique = Integer.parseInt(rawNb.replaceAll("[^0-9]", ""));
                            statut = firstEd.optString("statut", "");
                        }
                        break;
                    }
                }

                // 2. Calcul des possédés
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

                // 3. Préparation pour l'Adapter (Mode Collection)
                if (nbPossedes > 0) {
                    serie.put("nb_possedes", nbPossedes);
                    serie.put("nombre_tome_total", totalTheorique);
                    serie.put("statut", statut);
                    serie.put("affichage_collection", true); // Active la barre de progression
                    filteredList.add(serie);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        tvTomes.setText(countTotalTomes + " Tomes");
        tvSeries.setText(filteredList.size() + " Séries");

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new SerieAdapter(filteredList));

        return view;
    }

    // 1. Launcher pour le résultat du scan
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String isbnScanne = result.getContents();
                    rechercherMangaParISBN(isbnScanne);
                }
            });

    // 2. Launcher pour la permission Caméra
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    lancerScanner();
                } else {
                    Toast.makeText(getContext(), "Accès caméra refusé", Toast.LENGTH_SHORT).show();
                }
            });

    // 3. Fonction d'ajout
    private void lancerScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.EAN_13);
        options.setPrompt("Scannez le code ISBN du manga");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private void rechercherMangaParISBN(String isbn) {
        try {
            // 1. Charger la base de données complète des mangas
            InputStream is = getResources().openRawResource(R.raw.mangas);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONArray allMangas = new JSONArray(new String(buffer, StandardCharsets.UTF_8));

            JSONObject mangaTrouve = null;
            for (int i = 0; i < allMangas.length(); i++) {
                if (allMangas.getJSONObject(i).optString("ean").equals(isbn)) {
                    mangaTrouve = allMangas.getJSONObject(i);
                    break;
                }
            }

            if (mangaTrouve != null) {
                // 2. Ajouter automatiquement à la collection de l'utilisateur
                ajouterMangaALaCollectionAutomatique(mangaTrouve);

                // 3. Ouvrir la page pour montrer le succès
                Intent intent = new Intent(getContext(), MangaActivity.class);
                intent.putExtra("TITRE_MANGA", mangaTrouve.optString("titre_serie"));
                intent.putExtra("NUMERO_TOME", mangaTrouve.optInt("numero_tome"));
                startActivity(intent);

                Toast.makeText(getContext(), "Manga ajouté à la collection !", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Code ISBN inconnu", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("SCAN_AUTO", "Erreur : " + e.getMessage());
        }
    }

    private void ajouterMangaALaCollectionAutomatique(JSONObject mangaObj) {
        try {
            SharedPreferences pref = requireContext().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE);
            String email = pref.getString("user_email", null);
            if (email == null) return;

            File file = new File(requireContext().getFilesDir(), email + "_data.json");
            JSONObject userData = file.exists() ? new JSONObject(loadStringFromFile(file)) : new JSONObject();

            if (!userData.has("collection")) userData.put("collection", new JSONArray());
            JSONArray collection = userData.getJSONArray("collection");

            // Utilisation des noms de clés pour correspondre à votre logique de série
            String titreSerie = mangaObj.optString("titre_serie");
            int numTome = mangaObj.optInt("numero_tome");

            // Trouver ou créer la série
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

            // Trouver ou créer le tome
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

            // Action automatique : Possédé et Suivi
            tomeJson.put("posséder", true);
            tomeJson.put("souhaiter", true);
            tomeJson.put("jaquette", mangaObj.optString("image_url"));

            // Sauvegarde physique sur le téléphone
            saveStringToFile(file, userData.toString());

        } catch (Exception e) {
            Log.e("SAVE_AUTO", "Erreur sauvegarde : " + e.getMessage());
        }
    }

    private String loadStringFromFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return new String(data, StandardCharsets.UTF_8);
    }

    private void saveStringToFile(File file, String content) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }
}