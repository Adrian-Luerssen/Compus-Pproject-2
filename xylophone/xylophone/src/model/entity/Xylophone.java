package model.entity;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Xylophone
{
    public static final int XYLOPHONE_ID = 13;

    public static final int NUM_TILES = 8;
    public static final int OCTAVE_TILES = 6; /* Between 0-10 */

    /* Dictionaries */
    public static final int[] NOTE_TILES = {0, 2, 4, 5, 7, 9, 11, 12};
    public static final String[] KEY_TILES = { "C", "D", "E", "F", "G", "A", "B", "c"};
    public static final Color[] COLOR_TILES =
    {
        new Color(163, 82, 194),
        new Color(51, 99, 188),
        new Color(45, 148, 193),
        new Color(50, 126, 88),
        new Color(65, 152, 75),
        new Color(213, 187, 48),
        new Color(240, 108, 57),
        new Color(219, 67, 58)
    };

    private final Map<String, Integer> tilesNote;
    private final Map<String, Color> tilesColor;

    public Xylophone()
    {
        tilesNote = new HashMap<>();
        tilesColor = new HashMap<>();

        for (int i = 0; i < NUM_TILES; i++)
        {
            tilesNote.put(KEY_TILES[i], (OCTAVE_TILES * 12) + NOTE_TILES[i]);
            tilesColor.put(KEY_TILES[i], COLOR_TILES[i]);
        }
    }

    public int getNote(String key)
    {
        return tilesNote.get(key);
    }

    public Color getColor(String key)
    {
        return tilesColor.get(key);
    }
}
