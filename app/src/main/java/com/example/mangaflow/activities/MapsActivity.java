package com.example.mangaflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mangaflow.fragments.BaseFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mangaflow.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activité gérant l'affichage de la carte et la recherche de magasins de mangas/librairies.
 * Utilise Google Maps SDK et Google Places API.
 */
public class MapsActivity extends BaseFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RequestQueue requestQueue; // File d'attente pour les requêtes réseau Volley
    private EditText searchInput;

    // Clé API Google Cloud (nécessaire pour authentifier les requêtes Maps et Places)
    private final String API_KEY = "AIzaSyAR8e21t0_6aFiGX_J3aNFJ8yzIVoZzHCo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // 1. Initialisation de Volley pour effectuer des appels HTTP vers l'API Google Places
        requestQueue = Volley.newRequestQueue(this);

        // 2. Configuration de la barre de recherche (EditText)
        searchInput = findViewById(R.id.map_search_bar);

        // Détection de la validation du clavier (touche loupe ou "Entrée")
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                lancerRechercheManuelle();
                return true;
            }
            return false;
        });

        // 3. Chargement asynchrone de la carte Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // --- NAVIGATION : Listeners pour la barre de menu inférieure ---
        findViewById(R.id.btn_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });

        findViewById(R.id.btn_collection).setOnClickListener(v -> {
            startActivity(new Intent(this, CollectionActivity.class));
        });

        findViewById(R.id.btn_planning).setOnClickListener(v -> {
            startActivity(new Intent(this, PlanningActivity.class));
        });

        findViewById(R.id.btn_search).setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });

    }

    /**
     * Méthode appelée lorsque la carte est prête à être utilisée.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Positionnement par défaut sur Valenciennes avec un niveau de zoom de 13
        LatLng valenciennes = new LatLng(50.357, 3.523);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(valenciennes, 13f));

        // Écouteur qui détecte quand la carte arrête de bouger (scroll ou zoom fini)
        mMap.setOnCameraIdleListener(() -> {
            String texteRecherche = searchInput.getText().toString();

            // Recherche automatique des enseignes connues si la barre est vide
            if (texteRecherche.isEmpty()) {
                chercherMagasins(mMap.getCameraPosition().target, "fnac|cultura|librairie");
            } else {
                // Sinon, recherche basée sur le mot-clé saisi par l'utilisateur
                chercherMagasins(mMap.getCameraPosition().target, texteRecherche);
            }
        });
    }

    /**
     * Nettoie la carte et lance une recherche basée sur la saisie de l'utilisateur.
     */
    private void lancerRechercheManuelle() {
        String query = searchInput.getText().toString();
        if (!query.isEmpty()) {
            // Supprime les marqueurs existants pour ne pas encombrer la carte
            mMap.clear();
            // Recherche autour du point central actuel de la vue
            chercherMagasins(mMap.getCameraPosition().target, query);
        }
    }

    /**
     * Effectue une requête HTTP vers Google Places API pour trouver des lieux.
     * @param position Coordonnées GPS du centre de la recherche
     * @param motCle Terme de recherche (ex: "librairie")
     */
    private void chercherMagasins(LatLng position, String motCle) {
        // Construction de l'URL pour l'API Nearby Search (rayon de 5km)
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + position.latitude + "," + position.longitude +
                "&radius=5000" +
                "&keyword=" + motCle +
                "&key=" + API_KEY;

        // Création de la requête JSON
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Extraction des résultats du JSON de réponse
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            String name = place.getString("name");

                            // Récupération des coordonnées GPS du lieu trouvé
                            JSONObject loc = place.getJSONObject("geometry").getJSONObject("location");
                            LatLng poiLatLng = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

                            // Ajout d'un marqueur sur la carte pour chaque magasin
                            mMap.addMarker(new MarkerOptions()
                                    .position(poiLatLng)
                                    .title(name));
                        }
                    } catch (JSONException e) {
                        Log.e("MapsActivity", "Erreur JSON : " + e.getMessage());
                    }
                },
                error -> Log.e("MapsActivity", "Erreur API : " + error.toString())
        );

        // Ajout de la requête à la file d'attente Volley pour exécution
        requestQueue.add(request);
    }
}