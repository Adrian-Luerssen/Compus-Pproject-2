import controller.XylophoneController;
import model.XylophoneManager;
import view.XylophoneView;

import javax.swing.*;

public class Main
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            /* model-view-controller */
            XylophoneManager xylophoneManager = new XylophoneManager();
            XylophoneView xylophoneView = new XylophoneView();
            XylophoneController controller = new XylophoneController(xylophoneView, xylophoneManager);

            /* Setup */
            xylophoneView.registerController(controller);
            xylophoneManager.registerController(controller);

            xylophoneView.setVisible(true);

            /* TODO: Uncomment line bellow to implement optional 2 */
            xylophoneManager.runOptional();
        });
    }
}
