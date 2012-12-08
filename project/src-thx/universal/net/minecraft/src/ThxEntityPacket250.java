package net.minecraft.src;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ThxEntityPacket250
{
    String msg          = null;
    int entityId        = 0;
    int ownerId         = 0;
    int pilotId         = 0;
    int cmd_create_item = 0;
    int cmd_reload      = 0;
    int cmd_exit        = 0;
    int cmd_create_map  = 0;
    float posX          = 0f;
    float posY          = 0f;
    float posZ          = 0f;
    float yaw           = 0f;
    float pitch         = 0f;
    float roll          = 0f;
    float motionX       = 0f;
    float motionY       = 0f;
    float motionZ       = 0f;
    float damage        = 0f;
    float throttle      = 0f;
            
    public ThxEntityPacket250()
    {
    }
    
    public ThxEntityPacket250(Packet250CustomPayload packet)
    {
        applyPacket250CustomPayload(packet);
    }
    
    public void applyPacket250CustomPayload(Packet250CustomPayload packet)
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
    
    public Packet250CustomPayload createPacket250CustomPayload()
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
        StringBuffer sb = new StringBuffer("ThxEntityPacket250 { ");
        sb.append("msg: '").append(msg).append("', ");
        sb.append("entityId: ").append(entityId).append(", ");
        sb.append("ownerId: ").append(ownerId).append(", ");
        sb.append("pilotId: ").append(pilotId).append(", ");
        sb.append("posX: ").append(posX).append(", ");
        sb.append("posY: ").append(posY).append(", ");
        sb.append("posZ: ").append(posZ).append(", ");
        sb.append("yaw: ").append(yaw).append(", ");
        sb.append("pitch: ").append(pitch).append(", ");
        sb.append("roll: ").append(roll).append(", ");
        sb.append("motionX: ").append(motionX).append(", ");
        sb.append("motionY: ").append(motionY).append(", ");
        sb.append("motionZ: ").append(motionZ).append(", ");
        sb.append("damage: ").append(damage).append(", ");
        sb.append("throttle: ").append(throttle).append(" "); // no comma after last entry
        sb.append("}");
        return sb.toString();
    }
}
