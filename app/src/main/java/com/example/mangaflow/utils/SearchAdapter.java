package com.example.mangaflow.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mangaflow.R;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptateur universel pour les fonctionnalités de recherche.
 * Gère une liste complète (pour référence) et une liste affichée (filtrée).
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private List<JSONObject> listFull;    // Copie de sauvegarde de toutes les données
    private List<JSONObject> listDisplay; // Liste actuellement visible à l'écran
    private OnItemClickListener listener; // Interface pour gérer le clic
    private String fieldName;             // Clé JSON à afficher et filtrer (ex: "nom")

    /**
     * Interface de rappel (callback) pour communiquer le clic à l'activité/fragment.
     */
    public interface OnItemClickListener {
        void onItemClick(JSONObject data);
    }

    public SearchAdapter(List<JSONObject> items, String fieldName, OnItemClickListener listener) {
        // On crée des nouvelles listes pour éviter de modifier la source originale par référence
        this.listFull = new ArrayList<>(items);
        this.listDisplay = new ArrayList<>(items);
        this.fieldName = fieldName;
        this.listener = listener;
    }

    /**
     * Permet de réutiliser le même adaptateur pour une autre catégorie (ex: passer d'Auteurs à Éditeurs).
     */
    public void updateData(List<JSONObject> newItems, String newFieldName) {
        this.listFull = new ArrayList<>(newItems);
        this.listDisplay = new ArrayList<>(newItems);
        this.fieldName = newFieldName;
        notifyDataSetChanged(); // Rafraîchit totalement la liste
    }

    /**
     * Moteur de recherche interne.
     * @param text La chaîne de caractères saisie par l'utilisateur.
     */
    public void filter(String text) {
        listDisplay.clear();
        String query = text.toLowerCase().trim();

        for (JSONObject obj : listFull) {
            try {
                // Si la recherche est vide ou si le champ contient la requête, on l'ajoute
                if (query.isEmpty() || obj.getString(fieldName).toLowerCase().contains(query)) {
                    listDisplay.add(obj);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        // Informe le RecyclerView que les données ont changé pour qu'il se redessine
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Utilise un layout générique composé d'un simple TextView
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auteur, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        JSONObject data = listDisplay.get(position);
        try {
            // Affiche dynamiquement la valeur correspondant à fieldName
            holder.name.setText(data.getString(fieldName));
            // Renvoie l'objet JSON complet lors du clic
            holder.itemView.setOnClickListener(v -> listener.onItemClick(data));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public int getItemCount() {
        return listDisplay.size();
    }

    /**
     * Conteneur pour la vue d'une ligne de résultat.
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.textViewName);
        }
    }
}