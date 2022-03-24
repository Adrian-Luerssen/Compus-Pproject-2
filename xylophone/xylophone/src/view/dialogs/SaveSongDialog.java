package view.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static view.XylophoneView.*;

public class SaveSongDialog extends AbstractDialog implements ActionListener, MouseListener
{
    private static final String AC_SAVE = "AC_SAVE";

    private static final String PLACEHOLDER = "Your song name";
    private static final String TITLE_ERROR = "Song Save Error";
    private static final String MESSAGE_ERROR = "Enter song name";

    private JTextField nameTextField;
    private JButton saveButton;

    private String songName;
    private boolean isActive;

    public SaveSongDialog(Frame owner, String title)
    {
        super(owner, title);
        this.songName = "";
        this.isActive = false;

        configureView();
        this.pack();
        this.setLocationRelativeTo(owner);

        /* Remove focus from textField */
        this.saveButton.requestFocusInWindow();
    }

    public String getSongName()
    {
        return this.songName;
    }

    /* -------------------------------------- PANEL CONFIGURATION -------------------------------------- */

    @Override
    protected void configureView()
    {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        /* Center */
        JLabel nameLabel = createLabel("Name:", FONT_SIZE);
        saveButton = createButton("SAVE", AC_SAVE);
        saveButton.addActionListener(this);

        nameTextField = new JTextField(PLACEHOLDER);
        nameTextField.setFont(new Font("SansSerif", Font.PLAIN, FONT_SIZE));
        nameTextField.setForeground(PLACEHOLDER_COLOR);
        nameTextField.setColumns(20);
        nameTextField.setBorder(BorderFactory.createCompoundBorder
        (
                BorderFactory.createLineBorder(BUTTON_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10))
        );
        nameTextField.addMouseListener(this);

        JPanel serialPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        serialPanel.setBackground(BACKGROUND_COLOR);
        serialPanel.add(nameLabel);
        serialPanel.add(nameTextField);
        serialPanel.add(saveButton);

        dialogPanel.add(serialPanel, BorderLayout.CENTER);

        /* Padding */
        dialogPanel.add(createPadding(BACKGROUND_COLOR, 10, 0), BorderLayout.WEST);
        dialogPanel.add(createPadding(BACKGROUND_COLOR, 10, 0), BorderLayout.EAST);
        dialogPanel.add(createPadding(BACKGROUND_COLOR, 0, 10), BorderLayout.NORTH);
        dialogPanel.add(createPadding(BACKGROUND_COLOR, 0, 10), BorderLayout.SOUTH);

        this.add(dialogPanel);
    }

    /* -------------------------------------- VIEW UPDATE -------------------------------------- */

    @Override
    public boolean openDialog() {
        super.openDialog();

        return !songName.isBlank();
    }


    /* -------------------------------------- CONTROLLER -------------------------------------- */

    /* ActionListener */

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals(AC_SAVE))
        {
            if (!nameTextField.getText().isBlank() && !nameTextField.getText().equals(PLACEHOLDER)) {
                songName = nameTextField.getText();
                closeDialog();
                return;
            }

            new ErrorDialog((Frame) this.getOwner(), TITLE_ERROR, MESSAGE_ERROR).openDialog();
        }
    }

    /* MouseListener */

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (!isActive)
        {
            nameTextField.setText("");
            nameTextField.setForeground(FONT_COLOR);
            isActive = true;
        }
    }

    /* MouseListener (Ignored) */

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
