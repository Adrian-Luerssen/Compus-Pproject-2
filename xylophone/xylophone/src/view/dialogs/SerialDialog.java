package view.dialogs;

import model.entity.Serial;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static view.XylophoneView.*;

public class SerialDialog extends AbstractDialog implements ActionListener
{
    private static final String AC_PORT = "AC_PORT";
    private static final String AC_REFRESH = "AC_REFRESH";
    private static final String AC_CONNECT = "AC_CONNECT";

    private static final String TITLE_ERROR = "Serial Connection Error";
    private static final String[] MESSAGE_ERROR =
    {
            "Select a port first.",
            "Selected port is already open.",
            "Port opening failed, disconnect and try again."
    };

    private JComboBox<String> portsComboBox;

    private int result;

    public SerialDialog(Frame owner, String title)
    {
        super(owner, title);
        this.result = -1;

        configureView();
        this.pack();
        this.setLocationRelativeTo(owner);
    }

    /* -------------------------------------- PANEL CONFIGURATION -------------------------------------- */

    @Override
    protected void configureView() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        /* Center */
        JLabel portsLabel = createLabel("Ports:", FONT_SIZE);
        JButton refreshButton = createButton("REFRESH", AC_REFRESH);
        JButton connectButton = createButton("CONNECT", AC_CONNECT);
        refreshButton.addActionListener(this);
        connectButton.addActionListener(this);

        portsComboBox = new JComboBox<>();
        portsComboBox.addActionListener(this);
        portsComboBox.setActionCommand(AC_PORT);
        refreshPorts();

        JPanel serialPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        serialPanel.setBackground(BACKGROUND_COLOR);
        serialPanel.add(portsLabel);
        serialPanel.add(portsComboBox);
        serialPanel.add(refreshButton);
        serialPanel.add(connectButton);

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

        return result == 0;
    }

    private void refreshPorts()
    {
        String[] availablePortNames = Serial.getInstance().getAvailablePortNames();

        portsComboBox.setModel(new DefaultComboBoxModel<>(availablePortNames));
        portsComboBox.setEnabled(availablePortNames.length > 0);
    }

    /* -------------------------------------- CONTROLLER -------------------------------------- */

    @Override
    public void actionPerformed(ActionEvent e)
    {
        switch (e.getActionCommand())
        {
            case AC_REFRESH -> refreshPorts();
            case AC_CONNECT -> connectResponse();
            case AC_PORT -> Serial.getInstance().setPort((String) portsComboBox.getSelectedItem());
        }
    }

    private void connectResponse()
    {
        Serial serial = Serial.getInstance();
        result = serial.connect();

        if (result == 0)
        {
            closeDialog();
            serial.setConnected(true);
            return;
        }

        new ErrorDialog((Frame) this.getOwner(), TITLE_ERROR, MESSAGE_ERROR[result - 1]).openDialog();
    }

}
