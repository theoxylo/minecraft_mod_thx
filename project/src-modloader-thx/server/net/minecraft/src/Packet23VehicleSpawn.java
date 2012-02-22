package net.minecraft.src;

import java.io.*;

public class Packet23VehicleSpawn extends Packet
{
    public int entityId;
    public int xPosition;
    public int yPosition;
    public int zPosition;
    public int speedX;
    public int speedY;
    public int speedZ;
    public int type;
    public int throwerEntityId;

    public Packet23VehicleSpawn()
    {
    }

    public Packet23VehicleSpawn(Entity entity, int i)
    {
        this(entity, i, 0);
    }

    public Packet23VehicleSpawn(Entity entity, int i, int j)
    {
        System.out.println("Packet23VehicleSpawn called for entity " + entity + ", entityId " + entity.entityId + " with netId (entity type) " + i + " and owner/thrower entity id " + j);
        
        entityId = entity.entityId;
        xPosition = MathHelper.floor_double(entity.posX * 32D);
        yPosition = MathHelper.floor_double(entity.posY * 32D);
        zPosition = MathHelper.floor_double(entity.posZ * 32D);
        type = i;
        throwerEntityId = j;
        if (j > 0)
        {
            double d = entity.motionX;
            double d1 = entity.motionY;
            double d2 = entity.motionZ;
            double d3 = 3.8999999999999999D;
            if (d < -d3)
            {
                d = -d3;
            }
            if (d1 < -d3)
            {
                d1 = -d3;
            }
            if (d2 < -d3)
            {
                d2 = -d3;
            }
            if (d > d3)
            {
                d = d3;
            }
            if (d1 > d3)
            {
                d1 = d3;
            }
            if (d2 > d3)
            {
                d2 = d3;
            }
            speedX = (int)(d * 8000D);
            speedY = (int)(d1 * 8000D);
            speedZ = (int)(d2 * 8000D);
        }
    }

    public void readPacketData(DataInputStream datainputstream)
    throws IOException
    {
        entityId = datainputstream.readInt();
        type = datainputstream.readByte();
        xPosition = datainputstream.readInt();
        yPosition = datainputstream.readInt();
        zPosition = datainputstream.readInt();
        throwerEntityId = datainputstream.readInt();
        if (throwerEntityId > 0)
        {
            speedX = datainputstream.readShort();
            speedY = datainputstream.readShort();
            speedZ = datainputstream.readShort();
        }
    }

    public void writePacketData(DataOutputStream dataoutputstream)
    throws IOException
    {
        dataoutputstream.writeInt(entityId);
        dataoutputstream.writeByte(type);
        dataoutputstream.writeInt(xPosition);
        dataoutputstream.writeInt(yPosition);
        dataoutputstream.writeInt(zPosition);
        dataoutputstream.writeInt(throwerEntityId);
        if (throwerEntityId > 0)
        {
            dataoutputstream.writeShort(speedX);
            dataoutputstream.writeShort(speedY);
            dataoutputstream.writeShort(speedZ);
        }
    }

    public void processPacket(NetHandler nethandler)
    {
        nethandler.handleVehicleSpawn(this);
    }

    public int getPacketSize()
    {
        return 21 + throwerEntityId <= 0 ? 0 : 6;
    }
}
