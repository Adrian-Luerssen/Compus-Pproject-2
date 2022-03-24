package model.entity;

public class Note
{
    private String key;
    private int delay;  /* ms */

    public Note() {}

    public Note(String key, int delay)
    {
        this.key = key;
        this.delay = delay;
    }

    public String getKey()
    {
        return key;
    }

    public int getDelay()
    {
        return delay;
    }
}
