package com.example.mood_music;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private MediaPlayer mediaPlayer;
    private ProgressBar progressBar;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar); // Ensure this exists in activity_main.xml
        rvSongs = findViewById(R.id.rv_songs);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        songAdapter = new SongAdapter(this::playSong);
        rvSongs.setAdapter(songAdapter);

        setupMoodButtons();

        // Default mood
        fetchSongsFromServer("Happy");
    }

    private void setupMoodButtons() {
        findViewById(R.id.btn_happy).setOnClickListener(v -> fetchSongsFromServer("Happy"));
        findViewById(R.id.btn_sad).setOnClickListener(v -> fetchSongsFromServer("Sad"));
        findViewById(R.id.btn_angry).setOnClickListener(v -> fetchSongsFromServer("Rock"));
        findViewById(R.id.btn_relaxed).setOnClickListener(v -> fetchSongsFromServer("Ambient"));
        findViewById(R.id.btn_romantic).setOnClickListener(v -> fetchSongsFromServer("Love"));
        findViewById(R.id.btn_motivated).setOnClickListener(v -> fetchSongsFromServer("Workout"));
    }

    private void fetchSongsFromServer(String mood) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        executorService.execute(() -> {
            List<Song> songs = new ArrayList<>();
            try {
                // Using iTunes Search API as a free music server/API
                String urlString = "https://itunes.apple.com/search?term=" + mood + "&entity=song&limit=10";
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray results = jsonObject.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject track = results.getJSONObject(i);
                    String title = track.getString("trackName");
                    String artist = track.getString("artistName");
                    String previewUrl = track.getString("previewUrl");
                    songs.add(new Song(title, artist, previewUrl));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching songs", e);
            }

            mainHandler.post(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                songAdapter.updateData(songs);
                if (songs.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No songs found for " + mood, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void playSong(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        Toast.makeText(this, "Streaming: " + song.getTitle(), Toast.LENGTH_SHORT).show();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        try {
            mediaPlayer.setDataSource(song.getUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(MainActivity.this, "Error playing song", Toast.LENGTH_SHORT).show();
                return false;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting data source", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        executorService.shutdown();
    }
}