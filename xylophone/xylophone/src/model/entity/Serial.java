package model.entity;

import com.fazecast.jSerialComm.SerialPort;

import java.nio.ByteBuffer;

public class Serial
{
    /* Error codes */
    public static final int ERR_NO_PORT = 1;
    public static final int ERR_PORT_OPEN = 2;
    public static final int ERR_OPEN_FAIL = 3;

    private volatile static Serial serialSingleton;
    private static final int BAUDRATE = 9600;
    private static final int READ_DELAY_MS = 20;

    private SerialPort[] availablePorts;
    private SerialPort selectedPort;
    private volatile boolean isConnected;

    private Serial()
    {
        isConnected = false;
    }

    public static Serial getInstance()
    {
        if (serialSingleton == null)
        {
            synchronized (Serial.class)
            {
                if (serialSingleton == null)
                {
                    serialSingleton = new Serial();
                }
            }
        }

        return serialSingleton;
    }

    public void setPort(String portName)
    {
        for (SerialPort port : availablePorts)
        {
            if (port.getSystemPortName().equals(portName))
            {
                selectedPort = port;
            }
        }
    }

    public String[] getAvailablePortNames()
    {
        /* Update ports */
        availablePorts = SerialPort.getCommPorts();

        /* Get port names */
        String[] availablePortNames = new String[availablePorts.length + 1];
        availablePortNames[0] = "Available ports";

        for (int i = 1; i < availablePortNames.length; i++)
        {
            availablePortNames[i] = availablePorts[i - 1].getSystemPortName();
        }

        return availablePortNames;
    }

    public void setConnected(boolean isConnected)
    {
        this.isConnected = isConnected;
    }

    public boolean isConnected()
    {
        return isConnected;
    }

    public int connect()
    {
        if (selectedPort == null)
        {
            return ERR_NO_PORT;
        }

        if (selectedPort.isOpen())
        {
            return ERR_PORT_OPEN;
        }

        /* Non-blocking port */
        selectedPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        selectedPort.setBaudRate(BAUDRATE);

        return (selectedPort.openPort()) ? 0 : ERR_OPEN_FAIL;
    }

    /* -------------------------------------- SERIAL TRANSMISSION -------------------------------------- */

    public void sendCharacter(final char character)
    {
        byte[] sendData = new byte[1];
        sendData[0] = (byte) character;
        selectedPort.writeBytes(sendData, 1);
    }

    public char receiveNote()
    {
        byte[] receivedData = new byte[1];

        try
        {
            while (selectedPort.bytesAvailable() == 0)
            {
                Thread.sleep(READ_DELAY_MS);
            }

            selectedPort.readBytes(receivedData, 1);
        }
        catch (InterruptedException ignored) {}

        return (char) receivedData[0];
    }

    /* -------------------------------------- OPTIONAL 2 -------------------------------------- */

    public int receiveNoteDelay()
    {
        byte[] receivedData = new byte[2];

        try
        {
            while (selectedPort.bytesAvailable() < 2)
            {
                Thread.sleep(READ_DELAY_MS);
            }

            selectedPort.readBytes(receivedData, 2);
        }
        catch (InterruptedException ignored) {}

        return bytesToInt(receivedData);
    }


    private int bytesToInt(final byte[] receivedData)
    {
        byte[] bytes = new byte[] {0, 0, 0, 0};

        // TODO: change receiveData order depending on how delay is sent (MSB/LSB order)

        // First byte sent is LSB
        bytes[3] = receivedData[0];
        bytes[2] = receivedData[1];

        /*
        // First byte sent is MSB
        bytes[3] = receivedData[0];
        bytes[2] = receivedData[1];
        */

        return ByteBuffer.wrap(bytes).getInt();
    }
}
