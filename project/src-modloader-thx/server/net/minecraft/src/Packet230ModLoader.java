package net.minecraft.src;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Packet230ModLoader extends Packet
{
    private static final int MAX_DATA_LENGTH = 65535;
    public int modId;
    public int packetType;
    public int dataInt[];
    public float dataFloat[];
    public double dataDouble[];
    public String dataString[];
    private static Map playerMap = new HashMap();

    public Packet230ModLoader()
    {
        dataInt = new int[0];
        dataFloat = new float[0];
        dataDouble = new double[0];
        dataString = new String[0];
    }

    /**
     * Abstract. Reads the raw packet data from the data stream.
     */
    public void readPacketData(DataInputStream datainputstream) throws IOException
    {
        modId = datainputstream.readInt();
        packetType = datainputstream.readInt();
        int i = datainputstream.readInt();

        if (i > 65535)
        {
            throw new IOException(String.format("Integer data size of %d is higher than the max (%d).", new Object[]
                    {
                        Integer.valueOf(i), Integer.valueOf(65535)
                    }));
        }

        dataInt = new int[i];

        for (int j = 0; j < i; j++)
        {
            dataInt[j] = datainputstream.readInt();
        }

        int k = datainputstream.readInt();

        if (k > 65535)
        {
            throw new IOException(String.format("Float data size of %d is higher than the max (%d).", new Object[]
                    {
                        Integer.valueOf(k), Integer.valueOf(65535)
                    }));
        }

        dataFloat = new float[k];

        for (int l = 0; l < k; l++)
        {
            dataFloat[l] = datainputstream.readFloat();
        }

        int i1 = datainputstream.readInt();

        if (i1 > 65535)
        {
            throw new IOException(String.format("Double data size of %d is higher than the max (%d).", new Object[]
                    {
                        Integer.valueOf(i1), Integer.valueOf(65535)
                    }));
        }

        dataDouble = new double[i1];

        for (int j1 = 0; j1 < i1; j1++)
        {
            dataDouble[j1] = datainputstream.readDouble();
        }

        int k1 = datainputstream.readInt();

        if (k1 > 65535)
        {
            throw new IOException(String.format("String data size of %d is higher than the max (%d).", new Object[]
                    {
                        Integer.valueOf(k1), Integer.valueOf(65535)
                    }));
        }

        dataString = new String[k1];

        for (int l1 = 0; l1 < k1; l1++)
        {
            int i2 = datainputstream.readInt();

            if (i2 > 65535)
            {
                throw new IOException(String.format("String length of %d is higher than the max (%d).", new Object[]
                        {
                            Integer.valueOf(i2), Integer.valueOf(65535)
                        }));
            }

            byte abyte0[] = new byte[i2];

            for (int j2 = 0; j2 < i2; j2 += datainputstream.read(abyte0, j2, i2 - j2)) { }

            dataString[l1] = new String(abyte0);
        }
    }

    /**
     * Abstract. Writes the raw packet data to the data stream.
     */
    public void writePacketData(DataOutputStream dataoutputstream) throws IOException
    {
        if (dataInt != null && dataInt.length > 65535)
        {
            throw new IOException(String.format("Integer data size of %d is higher than the max (%d).", new Object[]
                    {
                        Integer.valueOf(dataInt.length), Integer.valueOf(65535)
                    }));
        }

        if (dataFloat != null && dataFloat.length > 65535)
        {
            throw new IOException(String.format("Float data size of %d is higher than the max (%d).", new Object[]
                    {
                        Integer.valueOf(dataFloat.length), Integer.valueOf(65535)
                    }));
        }

        if (dataDouble != null && dataDouble.length > 65535)
        {
            throw new IOException(String.format("Double data size of %d is higher than the max (%d).", new Object[]
                    {
                        Integer.valueOf(dataDouble.length), Integer.valueOf(65535)
                    }));
        }

        if (dataString != null && dataString.length > 65535)
        {
            throw new IOException(String.format("String data size of %d is higher than the max (%d).", new Object[]
                    {
                        Integer.valueOf(dataString.length), Integer.valueOf(65535)
                    }));
        }

        dataoutputstream.writeInt(modId);
        dataoutputstream.writeInt(packetType);

        if (dataInt == null)
        {
            dataoutputstream.writeInt(0);
        }
        else
        {
            dataoutputstream.writeInt(dataInt.length);

            for (int i = 0; i < dataInt.length; i++)
            {
                dataoutputstream.writeInt(dataInt[i]);
            }
        }

        if (dataFloat == null)
        {
            dataoutputstream.writeInt(0);
        }
        else
        {
            dataoutputstream.writeInt(dataFloat.length);

            for (int j = 0; j < dataFloat.length; j++)
            {
                dataoutputstream.writeFloat(dataFloat[j]);
            }
        }

        if (dataDouble == null)
        {
            dataoutputstream.writeInt(0);
        }
        else
        {
            dataoutputstream.writeInt(dataDouble.length);

            for (int k = 0; k < dataDouble.length; k++)
            {
                dataoutputstream.writeDouble(dataDouble[k]);
            }
        }

        if (dataString == null)
        {
            dataoutputstream.writeInt(0);
        }
        else
        {
            dataoutputstream.writeInt(dataString.length);

            for (int l = 0; l < dataString.length; l++)
            {
                if (dataString[l].length() > 65535)
                {
                    throw new IOException(String.format("String length of %d is higher than the max (%d).", new Object[]
                            {
                                Integer.valueOf(dataString[l].length()), Integer.valueOf(65535)
                            }));
                }

                dataoutputstream.writeInt(dataString[l].length());
                dataoutputstream.writeBytes(dataString[l]);
            }
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(NetHandler nethandler)
    {
        EntityPlayerMP entityplayermp = null;

        if (playerMap.containsKey(nethandler))
        {
            entityplayermp = (EntityPlayerMP)playerMap.get(nethandler);
        }
        else if (nethandler instanceof NetServerHandler)
        {
            try
            {
                entityplayermp = (EntityPlayerMP)ModLoader.getPrivateValue(net.minecraft.src.NetServerHandler.class, nethandler, 4);

                if (entityplayermp != null)
                {
                    playerMap.put(nethandler, entityplayermp);
                    System.out.println("Added player " + entityplayermp + " to playerMap NetHandler cache");
                }
            }
            catch (NoSuchFieldException nosuchfieldexception)
            {
                System.out.println("Error getting player from NetServerHandler.");
                nosuchfieldexception.printStackTrace();
            }
        }

        ModLoaderMp.handleAllPackets(this, entityplayermp);
    }

    /**
     * Abstract. Return the size of the packet (not counting the header).
     */
    public int getPacketSize()
    {
        int i = 0;
        i += 4;
        i += 4;
        i = (i += 4) + (dataInt == null ? 0 : dataInt.length * 4);
        i += 4;
        i = dataFloat == null ? 0 : dataFloat.length * 4;
        i += 4;
        i = dataDouble == null ? 0 : dataDouble.length * 8;
        i += 4;

        if (dataString != null)
        {
            for (int j = 0; j < dataString.length; j++)
            {
                i = (i += 4) + dataString[j].length();
            }
        }

        return i;
    }
}
