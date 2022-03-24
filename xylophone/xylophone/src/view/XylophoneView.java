package view;

import controller.XylophoneController;
import model.entity.Xylophone;
import view.dialogs.ErrorDialog;
import view.dialogs.SaveSongDialog;
import view.dialogs.SerialDialog;

import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class XylophoneView extends JFrame
{
    public static final String APP_NAME = "LSXylophone";

    public static final int FRAME_WIDTH = 1280;
    public static final int FRAME_HEIGHT = 720;

    public static final int ICON_SIZE = 48;
    public static final int HEADER_SIZE = 24;
    public static final int FONT_SIZE = 18;

    public static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
    public static final Color XYLOPHONE_COLOR = new Color(204, 204, 204);
    public static final Color BUTTON_COLOR = new Color(204, 204, 204);
    public static final Color FONT_COLOR = new Color(34, 34, 34);
    public static final Color PLACEHOLDER_COLOR = new Color(204, 204, 204);
    public static final Color STATUS_COLOR = new Color(109, 182, 101);

    public static final String MESSAGE_STATUS_CONNECTED = "Connected";
    public static final String MESSAGE_STATUS_PLAYING = "Connected (Playing)";
    public static final String MESSAGE_STATUS_RECORDING = "Connected (Recording)";

    public static final String AC_MUTE = "AC_MUTE";
    public static final String AC_FILE = "AC_FILE";
    public static final String AC_SERIAL = "AC_SERIAL";

    private static final String IC_VOLUME = "res/icon/volume.png";
    private static final String IC_VOLUME_MUTE = "res/icon/volume-mute.png";
    private static final String IC_FILE = "res/icon/folder.png";
    private static final String IC_SERIAL = "res/icon/serial.png";

    private JPanel mainView;
    private JButton[] tileButton;
    private JButton volumeButton;
    private JButton fileButton;
    private JButton serialButton;
    private JLabel statusLabel;
    private JLabel songLabel;

    public XylophoneView()
    {
        configureView();
        configureFrame();
    }

    /* -------------------------------------- FRAME CONFIGURATION -------------------------------------- */

    private void configureFrame()
    {
        this.pack();
        this.setTitle(APP_NAME);
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setContentPane(mainView);
    }

    /* -------------------------------------- PANEL CONFIGURATION -------------------------------------- */

    private void configureView()
    {
        mainView = new JPanel();
        mainView.setLayout(new BorderLayout());

        configureNorth();
        configureCenter();
        configureSouth();

        /* Padding */
        mainView.add(createPadding(BACKGROUND_COLOR, 50, 0), BorderLayout.WEST);
        mainView.add(createPadding(BACKGROUND_COLOR, 50, 0), BorderLayout.EAST);
    }

    private void configureNorth()
    {
        JPanel northPanel = new JPanel(new BorderLayout());

        /* Center */
        JLabel titleLabel = createLabel(APP_NAME, HEADER_SIZE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel = createLabel(MESSAGE_STATUS_CONNECTED, FONT_SIZE);
        statusLabel.setForeground(STATUS_COLOR);
        statusLabel.setVisible(false);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.add(titleLabel);
        headerPanel.add(statusLabel);
        northPanel.add(headerPanel, BorderLayout.CENTER);

        /* East */
        volumeButton = createIconButton(IC_VOLUME, AC_MUTE);
        fileButton = createIconButton(IC_FILE, AC_FILE);

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        configPanel.setBackground(BACKGROUND_COLOR);
        configPanel.add(fileButton);
        configPanel.add(volumeButton);
        configPanel.add(createPadding(BACKGROUND_COLOR, 10, 0));
        northPanel.add(configPanel, BorderLayout.EAST);

        /* West */
        serialButton = createIconButton(IC_SERIAL, AC_SERIAL);

        JPanel serialPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        serialPanel.setBackground(BACKGROUND_COLOR);
        serialPanel.add(createPadding(BACKGROUND_COLOR, 10, 0));
        serialPanel.add(serialButton);
        serialPanel.add(createPadding(BACKGROUND_COLOR, 30, 0));
        northPanel.add(serialPanel, BorderLayout.WEST);

        /* Padding */
        northPanel.add(createPadding(BACKGROUND_COLOR, 0, 20), BorderLayout.NORTH);
        northPanel.add(createPadding(BACKGROUND_COLOR, 0, 50), BorderLayout.SOUTH);

        mainView.add(northPanel, BorderLayout.NORTH);
    }

    private void configureCenter()
    {
        JPanel centerPanel = new JPanel(new BorderLayout());

        /* Center */
        JPanel xylophonePanel = new JPanel(new GridLayout(1, 8, 20, 0));
        xylophonePanel.setBackground(XYLOPHONE_COLOR);

        tileButton = new JButton[Xylophone.NUM_TILES];
        for (int i = 0; i < Xylophone.NUM_TILES; i++)
        {
            tileButton[i] = new JButton(Xylophone.KEY_TILES[i]);
            tileButton[i].setBorder(BorderFactory.createEmptyBorder());
            tileButton[i].setBackground(Xylophone.COLOR_TILES[i]);
            tileButton[i].setForeground(Color.WHITE);
            tileButton[i].setFont(new Font("SansSerif", Font.BOLD, 24));
            tileButton[i].setFocusPainted(false);
            xylophonePanel.add(tileButton[i]);
        }
        centerPanel.add(xylophonePanel, BorderLayout.CENTER);

        /* Padding */
        centerPanel.add(createPadding(XYLOPHONE_COLOR, 20, 0), BorderLayout.WEST);
        centerPanel.add(createPadding(XYLOPHONE_COLOR, 20, 0), BorderLayout.EAST);
        centerPanel.add(createPadding(XYLOPHONE_COLOR, 0, 20), BorderLayout.NORTH);
        centerPanel.add(createPadding(XYLOPHONE_COLOR, 0, 20), BorderLayout.SOUTH);

        mainView.add(centerPanel, BorderLayout.CENTER);
    }

    private void configureSouth()
    {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(BACKGROUND_COLOR);

        /* West */
        songLabel = createLabel("Current song: .", FONT_SIZE);

        JPanel songPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        songPanel.setBackground(BACKGROUND_COLOR);
        songPanel.add(createPadding(BACKGROUND_COLOR, 10, 0));
        songPanel.add(songLabel);
        southPanel.add(songPanel, BorderLayout.WEST);

        /* Padding */
        southPanel.add(createPadding(BACKGROUND_COLOR, 0, 50), BorderLayout.NORTH);
        southPanel.add(createPadding(BACKGROUND_COLOR, 0, 20), BorderLayout.SOUTH);

        mainView.add(southPanel, BorderLayout.SOUTH);
    }

    /* -------------------------------------- COMPONENTS FACTORY -------------------------------------- */

    public static JPanel createPadding(Color color, int width, int height)
    {
        JPanel border = new JPanel();
        border.setBackground(color);
        border.add(Box.createRigidArea( new Dimension(width, height)));
        return border;
    }

    public static JLabel createLabel(String text, int textSize)
    {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, textSize));
        label.setForeground(FONT_COLOR);
        return label;
    }

    public static JButton createButton(String text, String actionCommand)
    {
        JButton button = new JButton(text);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(FONT_COLOR);
        button.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setFocusPainted(false);
        button.setActionCommand(actionCommand);
        return button;
    }

    public static JLabel createIconLabel(String imgPath)
    {
        JLabel iconLabel = new JLabel(createImage(imgPath));
        iconLabel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        iconLabel.setMaximumSize(iconLabel.getPreferredSize());
        iconLabel.setBackground(BACKGROUND_COLOR);
        iconLabel.setBorder(BorderFactory.createEmptyBorder());
        return iconLabel;
    }

    public static JButton createIconButton(String imgPath, String actionCommand)
    {
        JButton iconButton = new JButton(createImage(imgPath));
        iconButton.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        iconButton.setMaximumSize(iconButton.getPreferredSize());
        iconButton.setBackground(BACKGROUND_COLOR);
        iconButton.setBorder(BorderFactory.createEmptyBorder());
        iconButton.setFocusPainted(false);
        iconButton.setActionCommand(actionCommand);
        return iconButton;
    }

    private static ImageIcon createImage(String imagePath)
    {
        ImageIcon resource = new ImageIcon(imagePath);
        Image image = resource.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /* -------------------------------------- CONTROLLER -------------------------------------- */

    public void registerController(XylophoneController controller)
    {
        for (JButton tile : tileButton)
        {
            tile.addActionListener(controller);
            tile.addMouseListener(controller);
        }

        volumeButton.addActionListener(controller);
        fileButton.addActionListener(controller);
        serialButton.addActionListener(controller);
    }

    /* -------------------------------------- DIALOGS -------------------------------------- */

    public String selectFile()
    {
        JFileChooser fileChooser = new JFileChooser("res/song");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Songs", "json"));

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            return fileChooser.getSelectedFile().getName();
        }

        return null;
    }

    public void displayError(String errorTitle, String errorMessage)
    {
        new ErrorDialog(this, errorTitle, errorMessage).openDialog();
    }

    public boolean configureSerial()
    {
        SerialDialog serialDialog = new SerialDialog(this, "Serial Configuration");
        return serialDialog.openDialog();
    }

    public String saveSong()
    {
        SaveSongDialog saveSongDialog = new SaveSongDialog(this, "Save Song");

        return (saveSongDialog.openDialog()) ? saveSongDialog.getSongName() : null;
    }

    /* -------------------------------------- VIEW UPDATE -------------------------------------- */

    public void swapVolumeIcon(boolean isMute)
    {
        volumeButton.setIcon((!isMute) ? createImage(IC_VOLUME) : createImage(IC_VOLUME_MUTE));
    }

    public void paintTile(String tileKey, Color color)
    {
        for (JButton tile : tileButton)
        {
            if (tile.getText().equals(tileKey))
            {
                tile.setBackground(color);
            }
        }
    }

    public void setTilesEnabled(boolean enable)
    {
        for (JButton tile : tileButton)
        {
            tile.setEnabled(enable);
        }
    }

    public void updateStatus(boolean status)
    {
        serialButton.setEnabled(!status);
        statusLabel.setVisible(status);
    }

    public void setPlaying(boolean isPlaying, String songName)
    {
        String currText = songLabel.getText();
        songLabel.setText(currText.substring(0, currText.indexOf(':') + 2) + songName + ".");

        String statusMessage = (isPlaying) ? MESSAGE_STATUS_PLAYING : MESSAGE_STATUS_CONNECTED;
        statusLabel.setText(statusMessage);

        setTilesEnabled(!isPlaying);
        fileButton.setEnabled(!isPlaying);
        serialButton.setEnabled(!isPlaying);
    }

    public void setRecording(boolean isRecording)
    {
        String statusMessage = (isRecording) ? MESSAGE_STATUS_RECORDING : MESSAGE_STATUS_CONNECTED;
        statusLabel.setText(statusMessage);

        fileButton.setEnabled(!isRecording);
    }
}
