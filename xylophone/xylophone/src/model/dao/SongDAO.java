package model.dao;

import model.entity.Song;

public interface SongDAO
{
    Song getSong(String songName);

    void saveSong(Song song);
}
