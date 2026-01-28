package com.example.mangaflow.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mangaflow.R;
import org.json.JSONObject;
import java.util.List;

public class PlanningAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0, TYPE_MANGA = 1;
    private List<Object> itemList;

    public PlanningAdapter(List<Object> itemList) { this.itemList = itemList; }

    @Override
    public int getItemViewType(int position) {
        return (itemList.get(position) instanceof String) ? TYPE_HEADER : TYPE_MANGA;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_planning_header, parent, false));
        }
        return new MangaVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manga_planning, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            HeaderVH h = (HeaderVH) holder;
            h.tvDate.setText((String) itemList.get(position));
        } else {
            JSONObject manga = (JSONObject) itemList.get(position);
            MangaVH h = (MangaVH) holder;

            String titreSerie = manga.optString("titre_serie", "Sans titre");
            h.tvTitle.setText(titreSerie);
            h.tvTome.setText("Tome " + manga.optString("numero_tome", "X"));

            Glide.with(h.itemView.getContext())
                    .load(manga.optString("image_url"))
                    .placeholder(R.drawable.placeholder_cover)
                    .centerCrop()
                    .into(h.ivCover);

            // --- AJOUT DE LA REDIRECTION ---
            h.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.mangaflow.activities.SerieActivity.class);
                // On envoie le nom de la série pour que SerieActivity charge les données
                intent.putExtra("SERIE_NAME", titreSerie);
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() { return itemList.size(); }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvDate;
        HeaderVH(View v) { super(v); tvDate = v.findViewById(R.id.tv_header_date); }
    }

    static class MangaVH extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvTome;
        MangaVH(View v) {
            super(v);
            ivCover = v.findViewById(R.id.iv_planning_cover);
            tvTitle = v.findViewById(R.id.tv_planning_title);
            tvTome = v.findViewById(R.id.tv_planning_tome);
        }
    }
}