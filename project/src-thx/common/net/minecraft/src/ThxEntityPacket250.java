package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ThxEntityPacket250 extends Packet250CustomPayload
{
    public int[] dataInt = new int[0];
    public float[] dataFloat = new float[0];
    public double[] dataDouble = new double[0];
    public String[] dataString = new String[0];

    public ThxEntityPacket250()
    {
	    channel = "THX_entity";
    }
    
    public void readPacketData(DataInputStream in) throws IOException
    {
        int intCount = in.readInt();
        dataInt = new int[intCount];
        for (int i = 0; i < intCount; i++)
        {
            dataInt[i] = in.readInt();
        }

        int floatCount = in.readInt();
        dataFloat = new float[floatCount];
        for (int i = 0; i < floatCount; i++)
        {
            dataFloat[i] = in.readFloat();
        }

        int doubleCount = in.readInt();
        dataDouble = new double[doubleCount];
        for (int i = 0; i < doubleCount; i++)
        {
            this.dataDouble[i] = in.readDouble();
        }

        int stringCount = in.readInt();
        dataString = new String[stringCount];
        for (int i = 0; i < stringCount; i++)
        {
            int stringLength = in.readInt();
            byte[] bytes = new byte[stringLength];

            for (int bytesRead = 0; bytesRead < stringLength; )
            {
                bytesRead += in.read(bytes, bytesRead, stringLength - bytesRead);
            }

            this.dataString[i] = new String(bytes);
        }
    }

    public void writePacketData(DataOutputStream out) throws IOException
    {
        if (this.dataInt == null)
        {
            out.writeInt(0);
        }
        else
        {
            out.writeInt(this.dataInt.length);

            for (int i = 0; i < this.dataInt.length; i++)
            {
                out.writeInt(this.dataInt[i]);
            }
        }

        if (this.dataFloat == null)
        {
            out.writeInt(0);
        }
        else
        {
            out.writeInt(this.dataFloat.length);

            for (int i = 0; i < this.dataFloat.length; i++)
            {
                out.writeFloat(this.dataFloat[i]);
            }
        }

        if (this.dataDouble == null)
        {
            out.writeInt(0);
        }
        else
        {
            out.writeInt(this.dataDouble.length);

            for (int i = 0; i < this.dataDouble.length; i++)
            {
                out.writeDouble(this.dataDouble[i]);
            }
        }

        if (this.dataString == null)
        {
            out.writeInt(0);
        }
        else
        {
            out.writeInt(this.dataString.length);

            for (int i = 0; i < this.dataString.length; i++)
            {
                out.writeInt(this.dataString[i].length());
                out.writeBytes(this.dataString[i]);
            }
        }
    }

    public int getPacketSize()
    {
        int size = 0;
        
        size += 4; // dataInt.length
        size += this.dataInt != null ? this.dataInt.length * 4 : 0;
        
        size += 4; // dataFloat.length
        size = this.dataFloat != null ? this.dataFloat.length * 4 : 0;
        
        size += 4; // dataDouble.length
        size = this.dataDouble != null ? this.dataDouble.length * 8 : 0;
        
        size += 4; // dataString.length
        if (this.dataString != null)
        {
            for (int i = 0; i < this.dataString.length; i++)
            {
                size += 4; // length of next string
                size += this.dataString[i].length();
            }
        }

        return size;
    }
}
