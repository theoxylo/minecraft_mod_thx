package net.minecraft.src;

import java.io.*;

public class Packet230ModLoader extends Packet
{
    private static final int MAX_DATA_LENGTH = 65535;
    public int modId;
    public int packetType;
    public int dataInt[];
    public float dataFloat[];
    public String dataString[];

    public Packet230ModLoader()
    {
        dataInt = new int[0];
        dataFloat = new float[0];
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
            throw new IOException(String.format("String data size of %d is higher than the max (%d).", new Object[]
                    {
                        Integer.valueOf(i1), Integer.valueOf(65535)
                    }));
        }

        dataString = new String[i1];

        for (int j1 = 0; j1 < i1; j1++)
        {
            int k1 = datainputstream.readInt();

            if (k1 > 65535)
            {
                throw new IOException(String.format("String length of %d is higher than the max (%d).", new Object[]
                        {
                            Integer.valueOf(k1), Integer.valueOf(65535)
                        }));
            }

            byte abyte0[] = new byte[k1];

            for (int l1 = 0; l1 < k1; l1 += datainputstream.read(abyte0, l1, k1 - l1)) { }

            dataString[j1] = new String(abyte0);
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

        if (dataString == null)
        {
            dataoutputstream.writeInt(0);
        }
        else
        {
            dataoutputstream.writeInt(dataString.length);

            for (int k = 0; k < dataString.length; k++)
            {
                if (dataString[k].length() > 65535)
                {
                    throw new IOException(String.format("String length of %d is higher than the max (%d).", new Object[]
                            {
                                Integer.valueOf(dataString[k].length()), Integer.valueOf(65535)
                            }));
                }

                dataoutputstream.writeInt(dataString[k].length());
                dataoutputstream.writeBytes(dataString[k]);
            }
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(NetHandler nethandler)
    {
        ModLoaderMp.HandleAllPackets(this);
    }

    /**
     * Abstract. Return the size of the packet (not counting the header).
     */
    public int getPacketSize()
    {
        int i = 1;
        i++;
        i = ++i + (dataInt != null ? dataInt.length * 32 : 0);
        i = ++i + (dataFloat != null ? dataFloat.length * 32 : 0);
        i++;

        if (dataString != null)
        {
            for (int j = 0; j < dataString.length; j++)
            {
                i = ++i + dataString[j].length();
            }
        }

        return i;
    }
    
    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append(getClass().getSimpleName()).append(" {");
        s.append("type: ").append(packetType).append(", ");
        s.append("modId: ").append(modId).append(", ");
        s.append("getPacketId(): ").append(getPacketId()).append(", ");

        for (int i = 0; dataInt != null && i < dataInt.length; i++)
        {
            s.append("dataInt[" + i + "]: ");
            s.append(dataInt[i]);
            s.append(", ");
        }
        for (int i = 0; dataFloat != null && i < dataFloat.length; i++)
        {
            s.append("dataFloat[" + i + "]: ");
            s.append(dataFloat[i]);
            s.append(", ");
        }
        for (int i = 0; dataString != null && i < dataString.length; i++)
        {
            s.append("dataString[" + i + "]: ");
            s.append(dataString[i]);
            s.append(", ");
        }
        s.append("}");

        return s.toString();
    } 
}
