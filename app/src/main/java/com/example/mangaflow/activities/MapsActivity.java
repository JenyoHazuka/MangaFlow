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

public class MapsActivity extends BaseFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RequestQueue requestQueue;
    private EditText searchInput;

    // Ta clé API configurée dans la console Google Cloud
    private final String API_KEY = "AIzaSyAR8e21t0_6aFiGX_J3aNFJ8yzIVoZzHCo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // 1. Initialisation de Volley
        requestQueue = Volley.newRequestQueue(this);

        // 2. Configuration de la barre de recherche
        // L'ID doit correspondre à celui de ton fichier activity_map.xml
        searchInput = findViewById(R.id.map_search_bar);

        // Détection de la touche "Entrée" ou "Recherche" sur le clavier
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                lancerRechercheManuelle();
                return true;
            }
            return false;
        });

        // 3. Chargement de la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // --- NAVIGATION ---
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Position initiale sur Valenciennes au démarrage
        LatLng valenciennes = new LatLng(50.357, 3.523);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(valenciennes, 13f));

        // AUTOMATISATION : Recherche par défaut quand on bouge la carte
        mMap.setOnCameraIdleListener(() -> {
            String texteRecherche = searchInput.getText().toString();

            // Si la barre est vide, on cherche les mangas/librairies par défaut
            if (texteRecherche.isEmpty()) {
                chercherMagasins(mMap.getCameraPosition().target, "fnac|cultura|librairie");
            } else {
                // Sinon on garde la recherche actuelle de l'utilisateur
                chercherMagasins(mMap.getCameraPosition().target, texteRecherche);
            }
        });
    }

    private void lancerRechercheManuelle() {
        String query = searchInput.getText().toString();
        if (!query.isEmpty()) {
            // On efface les anciens marqueurs pour ne voir que les nouveaux résultats
            mMap.clear();
            // On lance la recherche autour du centre actuel de la carte
            chercherMagasins(mMap.getCameraPosition().target, query);
        }
    }

    private void chercherMagasins(LatLng position, String motCle) {
        // Utilisation de l'API Places Nearby Search activée précédemment
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + position.latitude + "," + position.longitude +
                "&radius=5000" +
                "&keyword=" + motCle +
                "&key=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            String name = place.getString("name");
                            JSONObject loc = place.getJSONObject("geometry").getJSONObject("location");
                            LatLng poiLatLng = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

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

        requestQueue.add(request);
    }
}