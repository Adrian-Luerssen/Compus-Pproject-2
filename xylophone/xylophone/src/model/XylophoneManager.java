package model;

import controller.XylophoneController;
import model.dao.SongDAO;
import model.dao.json.JSONSongDAO;
import model.entity.*;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;

public class XylophoneManager
{
    private XylophoneController controller;
    private final SongDAO songDAO;

    private Synthesizer xylophoneSynth;
    private final Xylophone xylophoneData;

    public XylophoneManager()
    {
        songDAO = new JSONSongDAO();
        xylophoneData = new Xylophone();

        try
        {
            xylophoneSynth = MidiSystem.getSynthesizer();
            xylophoneSynth.open();

            /* Set instrument */
            xylophoneSynth.getChannels()[0].programChange(Xylophone.XYLOPHONE_ID);
        }
        catch (MidiUnavailableException ignored) {}
    }

    /* -------------------------------------- CONTROLLER -------------------------------------- */

    public void registerController(XylophoneController controller)
    {
        this.controller = controller;
    }

    /* -------------------------------------- MANUAL MODE -------------------------------------- */

    public void playNote(String noteKey)
    {
        Serial serial = Serial.getInstance();
        xylophoneSynth.getChannels()[0].noteOn(xylophoneData.getNote(noteKey), 127);

        if (serial.isConnected())
        {
            System.out.println("sending note... " + noteKey.charAt(0));
            serial.sendCharacter(noteKey.charAt(0));
        }
    }

    public void stopNote(String noteKey)
    {
        xylophoneSynth.getChannels()[0].noteOff(xylophoneData.getNote(noteKey));
    }

    public boolean mute()
    {
        MidiChannel channel = xylophoneSynth.getChannels()[0];
        channel.setMute(!channel.getMute());

        return channel.getMute();
    }

    /* -------------------------------------- AUTO MODE -------------------------------------- */

    public void playSong(String songName) {
        if (songName != null)
        {
            Serial serial = Serial.getInstance();
            Song song = songDAO.getSong(songName.substring(0, songName.indexOf('.')));

            Thread songThread = new Thread(() ->
            {
                controller.playSong(song.getName());

                /* Notify start of song */
                if (serial.isConnected()) {
                    char note;

                    serial.sendCharacter('P');
                    note = serial.receiveNote();

                    if (note != 'K') {
                        /* Wrong ACK, end song execution */
                        String errorMessage = "Received character was not ACK (K), aborting execution";

                        if (note == 'T') {
                            errorMessage = "Serial timeout, no character was received, aborting execution";
                        }

                        controller.displayError("Serial Transmission Error", errorMessage);
                        controller.endSong();
                        return;
                    }
                }

                /* Play song */
                for (Note note : song.getNotes()) {
                    try {
                        Thread.sleep((long) (note.getDelay() * 0.75));

                        controller.paintTile(note.getKey(), xylophoneData.getColor(note.getKey()));
                        playNote(note.getKey());
                        stopNote(note.getKey());
                    }
                    catch (InterruptedException ignored) {}
                }

                /* Notify end of song */
                if (serial.isConnected()) {
                    serial.sendCharacter('S');
                }

                controller.endSong();
            });

            songThread.start();
        }
    }

    /* -------------------------------------- OPTIONAL 2 -------------------------------------- */

    public void runOptional()
    {
        new Thread(() ->
        {
            Serial serial = Serial.getInstance();

            while (true)
            {
                if (serial.isConnected())
                {
                    if (serial.receiveNote() == 'P')
                    {
                        controller.startRecording();
                        receiveSong();
                        controller.endRecording();
                    }
                }
            }
        });
    }

    public void receiveSong()
    {
        char note;
        int noteDelay;

        String songName;
        List<Note> songNotes = new ArrayList<>();

        Serial serial = Serial.getInstance();

        /* Send ACK */
        serial.sendCharacter('K');

        /* Receive notes until S is received */
        while (true)
        {
            note = serial.receiveNote();

            if (note == 'S')
            {
                /* End of song */
                songName = controller.getRecordingName();
                break;
            }

            noteDelay = serial.receiveNoteDelay();
            songNotes.add(new Note(String.valueOf(note), noteDelay));
        }

        if (songName != null)
        {
            songDAO.saveSong(new Song(songName, songNotes));
        }
    }
}
