package net.minecraft.src;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ThxEntityPacket250Data
{
    public String msg          = null;
    public int entityId        = 0;
    public int ownerId         = 0;
    public int pilotId         = 0;
    public int cmd_create_item = 0;
    public int cmd_reload      = 0;
    public int cmd_exit        = 0;
    public int cmd_create_map  = 0;
    public float posX          = 0f;
    public float posY          = 0f;
    public float posZ          = 0f;
    public float yaw           = 0f;
    public float pitch         = 0f;
    public float roll          = 0f;
    public float motionX       = 0f;
    public float motionY       = 0f;
    public float motionZ       = 0f;
    public float damage        = 0f;
    public float throttle      = 0f;
            
    public ThxEntityPacket250Data()
    {
    }
    
    public ThxEntityPacket250Data(Packet250CustomPayload packet)
    {
        populate(packet);
    }
    
    public ThxEntityPacket250Data(ThxEntity entity)
    {
        //if (entity instanceof ThxEntityProjectile) entity.log("creating ThxEntityPacket250Data for entity " + entity);
        
        populate(entity);
    }
    
    public void populate(Packet250CustomPayload packet)
    {
	    if (!"THX_entity".equals(packet.channel)) throw new IllegalArgumentException("Incorrect packet channel: " + packet.channel);;
        
        clear();
        
        if (packet.data == null || packet.data.length == 0) return;
        
        try
        {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.data));
            
            msg = in.readUTF();
            
            entityId = in.readInt();
            ownerId  = in.readInt();
            pilotId  = in.readInt();
            
            cmd_create_item = in.readInt();
            cmd_reload      = in.readInt();
            cmd_exit        = in.readInt();
            cmd_create_map  = in.readInt();
           
            posX     = in.readFloat();
            posY     = in.readFloat();
            posZ     = in.readFloat();
            yaw      = in.readFloat();
            pitch    = in.readFloat();
            roll     = in.readFloat();
            motionX  = in.readFloat();
            motionY  = in.readFloat();
            motionZ  = in.readFloat();
            damage   = in.readFloat();
            throttle = in.readFloat();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
	}
    
    public ThxEntityPacket250Data populate(ThxEntity entity)
    {
        entityId = entity.entityId;
        ownerId = entity.owner != null ? entity.owner.entityId : 0;
        pilotId = entity.riddenByEntity != null ? entity.riddenByEntity.entityId : 0;

        if (entity.worldObj.isRemote) 
        {
            msg = "client timestamp: " + entity.worldObj.getWorldTime();

            // don't send any server commands to client as they are not used there

            cmd_create_item = 0;
            cmd_reload      = 0;
            cmd_exit        = 0;
            cmd_create_map  = 0;
        }
        else
        {
            msg = "server timestamp: " + entity.worldObj.getWorldTime();

            // clear cmd flags after setting them in packet to avoid resending them later

            cmd_create_item = entity.cmd_create_item;
            entity.cmd_create_item = 0;

            cmd_reload      = entity.cmd_reload;
            entity.cmd_reload = 0;

            cmd_exit        = entity.cmd_exit;
            entity.cmd_exit = 0;

            cmd_create_map  = entity.cmd_create_map;
            entity.cmd_create_map = 0;
        }

        posX            = (float) entity.posX;
        posY            = (float) entity.posY;
        posZ            = (float) entity.posZ;
        yaw             = entity.rotationYaw;
        pitch           = entity.rotationPitch;
        roll            = entity.rotationRoll;
        motionX         = (float) entity.motionX;
        motionY         = (float) entity.motionY;
        motionZ         = (float) entity.motionZ;
        throttle        = entity.throttle;
        damage          = entity.damage;
        
        return this;
    }
    
    public void clear() // reset all values to allow for reuse of this instance
    {
	    msg             = null;
	    entityId        = 0;
	    ownerId         = 0;
	    pilotId         = 0;
	    cmd_create_item = 0;
	    cmd_reload      = 0;
	    cmd_exit        = 0;
	    cmd_create_map  = 0;
	    posX            = 0f;
	    posY            = 0f;
	    posZ            = 0f;
	    yaw             = 0f;
	    pitch           = 0f;
	    roll            = 0f;
	    motionX         = 0f;
	    motionY         = 0f;
	    motionZ         = 0f;
	    damage          = 0f;
	    throttle        = 0f;
    }
    
    public Packet250CustomPayload createPacket250()
    {
	    ByteArrayOutputStream baos = null;
	    try
	    {
	        baos = new ByteArrayOutputStream();
	        DataOutputStream out = new DataOutputStream(baos);
	        
	        out.writeUTF(msg != null ? msg : "");
	        out.writeInt(entityId);
	        out.writeInt(ownerId);
	        out.writeInt(pilotId); // riddenByEntity
	        out.writeInt(cmd_create_item);
	        out.writeInt(cmd_reload);
	        out.writeInt(cmd_exit);
	        out.writeInt(cmd_create_map);
	        out.writeFloat(posX);
	        out.writeFloat(posY);
	        out.writeFloat(posZ);
	        out.writeFloat(yaw);
	        out.writeFloat(pitch);
	        out.writeFloat(roll);
	        out.writeFloat(motionX);
	        out.writeFloat(motionY);
	        out.writeFloat(motionZ);
	        out.writeFloat(damage);
	        out.writeFloat(throttle);
	    }
	    catch (IOException e)
	    {
	        throw new RuntimeException(e);
	    }
	    return new Packet250CustomPayload("THX_entity", baos.toByteArray());
    }

    @Override
    public String toString()
    {        
        return String.format("ThxEntityPacket250Data %s, entity %d, owner %d, pilot %d [posX: %6.2f, posY: %6.2f, posZ: %6.2f, yaw: %6.2f, pitch: %6.2f, roll: %6.2f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f, throttle: %6.3f, damage: %d]", msg, entityId, ownerId, pilotId, posX, posY, posZ, yaw, pitch, roll, motionX, motionY, motionZ, throttle, (int) damage);
    }
}
