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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private List<JSONObject> listFull;
    private List<JSONObject> listDisplay;
    private OnItemClickListener listener;
    private String fieldName;

    public interface OnItemClickListener {
        void onItemClick(JSONObject data);
    }

    public SearchAdapter(List<JSONObject> items, String fieldName, OnItemClickListener listener) {
        this.listFull = new ArrayList<>(items);
        this.listDisplay = new ArrayList<>(items);
        this.fieldName = fieldName;
        this.listener = listener;
    }

    public void updateData(List<JSONObject> newItems, String newFieldName) {
        this.listFull = new ArrayList<>(newItems);
        this.listDisplay = new ArrayList<>(newItems);
        this.fieldName = newFieldName;
        notifyDataSetChanged();
    }

    public void filter(String text) {
        listDisplay.clear();
        String query = text.toLowerCase().trim();
        for (JSONObject obj : listFull) {
            try {
                if (query.isEmpty() || obj.getString(fieldName).toLowerCase().contains(query)) {
                    listDisplay.add(obj);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auteur, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        JSONObject data = listDisplay.get(position);
        try {
            holder.name.setText(data.getString(fieldName));
            holder.itemView.setOnClickListener(v -> listener.onItemClick(data));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public int getItemCount() { return listDisplay.size(); }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.textViewName);
        }
    }
}