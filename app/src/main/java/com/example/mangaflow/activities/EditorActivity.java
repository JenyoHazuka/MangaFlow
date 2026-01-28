package com.example.mangaflow.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import com.example.mangaflow.utils.EditorAdapter;
import com.example.mangaflow.models.EditorItem;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Activité affichant le catalogue complet d'un éditeur.
 * Les œuvres sont classées par type (Mangas, LN, Artbooks) et triées par ordre alphabétique.
 */
public class EditorActivity extends BaseActivity {

    private RecyclerView recyclerView;
    // Liste hybride contenant des objets EditorItem (peuvent être des en-têtes ou des mangas)
    private List<EditorItem> displayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Récupération des données JSON complètes envoyées par SearchActivity ou SerieActivity
        String jsonStr = getIntent().getStringExtra("DATA_JSON");
        String nomEditeur = "Éditeur"; // Valeur par défaut

        try {
            if (jsonStr != null) {
                JSONObject data = new JSONObject(jsonStr);
                nomEditeur = data.getString("nom");

                /* TRAITEMENT DES DONNÉES :
                   On transforme les tableaux JSON bruts en une liste d'objets EditorItem
                   structurée pour l'affichage.
                */
                processSection("MANGAS", data.optJSONArray("mangas"));
                processSection("LIGHT NOVELS", data.optJSONArray("light_novels"));
                processSection("ARTBOOKS", data.optJSONArray("artbooks"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Mise à jour du titre de la page avec le nom de l'éditeur
        TextView tvHeader = findViewById(R.id.editor_name);
        if (tvHeader != null) tvHeader.setText(nomEditeur);

        // Configuration du RecyclerView (Liste simple verticale)
        recyclerView = findViewById(R.id.recycler_mangas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        /* L'EditorAdapter reçoit notre displayList qui contient maintenant :
           [HEADER: MANGAS, ITEM: A, ITEM: B, HEADER: LN, ITEM: C...]
        */
        recyclerView.setAdapter(new EditorAdapter(displayList));

        // Bouton retour pour fermer l'activité
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Navigation globale
        setupNavigation();
    }

    /**
     * Extrait les données d'un tableau JSON, les trie et les injecte dans la liste d'affichage.
     * * @param sectionTitle Le nom de la catégorie (ex: "MANGAS")
     * @param array Le tableau JSON contenant les titres des œuvres
     */
    private void processSection(String sectionTitle, JSONArray array) throws Exception {
        // On ne traite la section que si elle contient au moins une œuvre
        if (array != null && array.length() > 0) {
            List<String> tempList = new ArrayList<>();

            // 1. Conversion du JSONArray en List<String> pour faciliter le tri
            for (int i = 0; i < array.length(); i++) {
                tempList.add(array.getString(i));
            }

            // 2. TRI ALPHABÉTIQUE (A-Z) en ignorant les différences majuscules/minuscules
            Collections.sort(tempList, String.CASE_INSENSITIVE_ORDER);

            // 3. CRÉATION DE LA STRUCTURE VISUELLE
            // On ajoute d'abord un élément de type "HEADER" (titre de la section)
            displayList.add(new EditorItem(EditorItem.TYPE_HEADER, sectionTitle));

            // On ajoute ensuite tous les titres triés en tant qu'éléments de type "MANGA"
            for (String title : tempList) {
                displayList.add(new EditorItem(EditorItem.TYPE_MANGA, title));
            }
        }
    }

    /**
     * Initialisation des liens de navigation vers les autres parties de l'app.
     */
    private void setupNavigation() {
        findViewById(R.id.btn_home).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }
}