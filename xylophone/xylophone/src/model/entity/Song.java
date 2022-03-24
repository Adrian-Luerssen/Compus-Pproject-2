package model.entity;

import java.util.ArrayList;
import java.util.List;

public class Song
{
    private String name;
    private List<Note> notes;

    public Song() {

    }

    public Song(String name, List<Note> notes)
    {
        this.name = name;
        this.notes = new ArrayList<>(notes);
    }

    public String getName()
    {
        return name;
    }

    public List<Note> getNotes()
    {
        return notes;
    }
}
