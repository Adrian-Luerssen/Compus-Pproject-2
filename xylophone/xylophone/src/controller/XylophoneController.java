package controller;

import model.XylophoneManager;
import view.XylophoneView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class XylophoneController implements ActionListener, MouseListener
{
    XylophoneView xylophoneView;
    XylophoneManager xylophoneManager;

    public XylophoneController(XylophoneView xylophoneView, XylophoneManager xylophoneManager)
    {
        this.xylophoneView = xylophoneView;
        this.xylophoneManager = xylophoneManager;
    }

    /* -------------------------------------- MODEL-VIEW -------------------------------------- */

    public void paintTile(String tileKey, Color color)
    {
        Thread paintThread = new Thread(() ->
        {
            try
            {
                xylophoneView.paintTile(tileKey, Color.DARK_GRAY);
                Thread.sleep(300);
                xylophoneView.paintTile(tileKey, color);
            }
            catch (InterruptedException ignored) {}
        });

        paintThread.start();
    }

    public void displayError(String errorTitle, String errorMessage)
    {
        xylophoneView.displayError(errorTitle, errorMessage);
    }

    public void playSong(String songName)
    {
        xylophoneView.setPlaying(true, songName);
    }

    public void endSong()
    {
        xylophoneView.setPlaying(false, "");
    }

    public void startRecording()
    {
        xylophoneView.setRecording(true);
    }

    public void endRecording()
    {
        xylophoneView.setRecording(false);
    }

    public String getRecordingName()
    {
        return xylophoneView.saveSong();
    }

    /* -------------------------------------- VIEW-MODEL -------------------------------------- */

    /* ActionListener */

    @Override
    public void actionPerformed(ActionEvent e)
    {
        switch (e.getActionCommand())
        {
            case XylophoneView.AC_MUTE -> xylophoneView.swapVolumeIcon(xylophoneManager.mute());
            case XylophoneView.AC_FILE -> xylophoneManager.playSong(xylophoneView.selectFile());
            case XylophoneView.AC_SERIAL -> xylophoneView.updateStatus(xylophoneView.configureSerial());
        }
    }

    /* MouseListener */

    @Override
    public void mousePressed(MouseEvent e)
    {
        JButton tileButton = ((JButton) e.getSource());

        if (tileButton.isEnabled())
        {
            xylophoneManager.playNote(tileButton.getText());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        JButton tileButton = ((JButton) e.getSource());

        if (tileButton.isEnabled())
        {
            xylophoneManager.stopNote(tileButton.getText());
        }
    }

    /* MouseListener (Ignored) */

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
