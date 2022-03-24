package view.dialogs;

import javax.swing.*;
import java.awt.*;

import static view.XylophoneView.*;

public class ErrorDialog extends AbstractDialog
{
    private static final String IC_ERROR = "res/icon/error.png";

    private final String message;

    public ErrorDialog(Frame owner, String title, String message)
    {
        super(owner, title);

        this.message = message;

        configureView();
        this.pack();
        this.setLocationRelativeTo(owner);
    }

    /* -------------------------------------- PANEL CONFIGURATION -------------------------------------- */

    protected void configureView()
    {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        /* Center */
        JLabel iconLabel = createIconLabel(IC_ERROR);
        JLabel messageLabel = createLabel(this.message, FONT_SIZE);

        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        messagePanel.setBackground(BACKGROUND_COLOR);
        messagePanel.add(iconLabel);
        messagePanel.add(messageLabel);

        dialogPanel.add(messagePanel, BorderLayout.CENTER);

        /* Padding */
        dialogPanel.add(createPadding(BACKGROUND_COLOR, 10, 0), BorderLayout.WEST);
        dialogPanel.add(createPadding(BACKGROUND_COLOR, 10, 0), BorderLayout.EAST);
        dialogPanel.add(createPadding(BACKGROUND_COLOR, 0, 10), BorderLayout.NORTH);
        dialogPanel.add(createPadding(BACKGROUND_COLOR, 0, 10), BorderLayout.SOUTH);

        this.add(dialogPanel);
    }
}
