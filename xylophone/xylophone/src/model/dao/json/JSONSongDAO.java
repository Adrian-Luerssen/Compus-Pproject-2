package model.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.dao.SongDAO;
import model.entity.Song;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONSongDAO implements SongDAO
{
    @Override
    public Song getSong(String songName)
    {
        Gson gson = new Gson();

        String path = "res/song/" + songName + ".json";
        Song song = null;

        try (FileReader reader = new FileReader(path))
        {
            song = gson.fromJson(reader, Song.class);
        }
        catch (IOException ignored) {}

        return song;
    }

    @Override
    public void saveSong(Song song)
    {
        String path = "res/song/" + song.getName() + ".json";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(path))
        {
            gson.toJson(song, writer);
        }
        catch (IOException ignored) {}
    }
}
