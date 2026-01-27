package com.example.mangaflow.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangaflow.R;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private List<String> listFull; // Liste complète
    private List<String> listDisplay; // Liste affichée (filtrée)

    public SearchAdapter(List<String> items) {
        this.listFull = new ArrayList<>(items);
        this.listDisplay = new ArrayList<>(items);
    }

    // Méthode pour changer les données quand on change de catégorie
    public void updateData(List<String> newItems) {
        this.listFull = new ArrayList<>(newItems);
        this.listDisplay = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        listDisplay.clear();
        if (text.isEmpty()) {
            listDisplay.addAll(listFull);
        } else {
            String query = text.toLowerCase().trim();
            for (String item : listFull) {
                if (item.toLowerCase().contains(query)) {
                    listDisplay.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Utilise item_auteur.xml créé précédemment
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auteur, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(listDisplay.get(position));
    }

    @Override
    public int getItemCount() {
        return listDisplay.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.textViewName);
        }
    }
}