package net.minecraft.src;

//import net.minecraft.client.Minecraft;

public abstract class ThxEntity extends ThxEntityBase implements ISpawnable
{
    Packet230ModLoader latestUpdatePacket;
    
    public ThxEntity(World world)
    {
        super(world);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        applyUpdatePacketFromClient();    

        updateRotation();
        updateVectors();
    }
    
    /** ISpawnable server interface */
    @Override
    public Packet230ModLoader getSpawnPacket()
    {
        Packet230ModLoader packet = getUpdatePacket();
        packet.dataString[0] = "thx spawn packet for entity " + entityId;
        log("Created new spawn packet: " + packet);
        return packet;
    }
        
    public Packet230ModLoader getUpdatePacket()
    {
        log("getUpdatePacket()");
        
        Packet230ModLoader packet = new Packet230ModLoader();
        
        packet.modId = mod_Thx.instance.getId();
        packet.packetType = 75; // entity.entityNetId;
        
        packet.dataString = new String[]{ "thx update packet for tick " + ticksExisted };
        
        packet.dataInt = new int[2];
        packet.dataInt[0] = entityId;
        packet.dataInt[1] = riddenByEntity != null ? riddenByEntity.entityId : 0;
        
        packet.dataFloat = new float[9];
        packet.dataFloat[0] = (float) posX;
        packet.dataFloat[1] = (float) posY;
        packet.dataFloat[2] = (float) posZ;
        packet.dataFloat[3] = rotationYaw;
        packet.dataFloat[4] = rotationPitch;
        packet.dataFloat[5] = rotationRoll;
        packet.dataFloat[6] = (float) motionX;
        packet.dataFloat[7] = (float) motionY;
        packet.dataFloat[8] = (float) motionZ;
        
        return packet;
    }
    
    public void handleUpdatePacketFromClient(Packet230ModLoader packet)
    {
        if (packet.dataInt == null || packet.dataInt.length != 2)
        {
            log("Ignoring update packet without entity IDs");
            return;
        }
        if (entityId != packet.dataInt[0])
        {
            log("Ignoring update packet with wrong entity id " + packet.dataInt[0]);
            return;
        }
        if (riddenByEntity == null)
        {
            log("Ignoring update packet since we have no pilot");
            return;
        }
        if (riddenByEntity.entityId != packet.dataInt[1])
        {
            log("Ignoring update packet with wrong pilot id " + packet.dataInt[1]);
            return;
        }
        
        log("handleUpdatePacket - posX: " + packet.dataFloat[0] + ", posY: " + packet.dataFloat[1] + ", posZ: " + packet.dataFloat[2]);
        
        latestUpdatePacket = packet;
    }
    
    private void applyUpdatePacketFromClient()
    {
        plog("applyUpdatePacketFromClient(), latest packet: " + latestUpdatePacket);
        
        if (latestUpdatePacket == null) return;
        
        Packet230ModLoader p = latestUpdatePacket;
        //latestUpdatePacket = null; uncomment to only apply each packet once
        
        setPosition(p.dataFloat[0], p.dataFloat[1], p.dataFloat[2]);
        setRotation(p.dataFloat[3], p.dataFloat[4]);
        
        rotationRoll  = p.dataFloat[5] % 360f;
        
        // for now, clear any motion
        motionX       = .0; //p.dataFloat[6];
        motionY       = .0; //p.dataFloat[7];
        motionZ       = .0; //p.dataFloat[8];
        
        
        if (riddenByEntity == null)
        {
            System.out.println("update received even though no pilot");
            latestUpdatePacket = null;
        }
        else if (riddenByEntity.isDead)
        {
	        // last update for this player packet if player is dead
            log ("pilot entity is dead");
            riddenByEntity.mountEntity(this);
            latestUpdatePacket = null;
        }
        else 
        {
            updateRiderPosition();
        }
    }

}
