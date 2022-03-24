package view.dialogs;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractDialog extends JDialog
{
    private final String title;

    public AbstractDialog(Frame owner, String title)
    {
        super(owner);
        this.title = title;

        configureDialog();
    }

    /* -------------------------------------- DIALOG CONFIGURATION -------------------------------------- */

    private void configureDialog()
    {
        this.setTitle(this.title);
        this.setResizable(true);

        /* Block other windows */
        this.setModalityType(ModalityType.APPLICATION_MODAL);
    }

    /* -------------------------------------- PANEL CONFIGURATION -------------------------------------- */

    /* Call pack() after configureView() */
    protected abstract void configureView();

    /* -------------------------------------- VIEW UPDATE -------------------------------------- */

    public boolean openDialog()
    {
        this.setVisible(true);
        return true;
    }

    public void closeDialog()
    {
        this.setVisible(false);
    }
}
