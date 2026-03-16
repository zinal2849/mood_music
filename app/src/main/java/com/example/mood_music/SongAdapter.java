package com.example.mood_music;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    private List<Song> songs = new ArrayList<>();
    private final OnSongClickListener listener;

    public SongAdapter(OnSongClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CHANGE THIS: Replace 'R.layout.item_song' with your actual layout file name
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());

        holder.itemView.setOnClickListener(v -> listener.onSongClick(song));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist;

        SongViewHolder(View itemView) {
            super(itemView);
            // These IDs now match the XML above
            tvTitle = itemView.findViewById(R.id.textViewSongTitle);
            tvArtist = itemView.findViewById(R.id.textViewArtistName);
        }
    }


}