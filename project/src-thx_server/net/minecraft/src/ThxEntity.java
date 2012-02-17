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
        //log("getUpdatePacket()");
        
        Packet230ModLoader packet = new Packet230ModLoader();
        
        packet.modId = mod_Thx.instance.getId();
        packet.packetType = 75; // entity.entityNetId;
        
        packet.dataString = new String[]{ "thx update packet for tick " + ticksExisted };
        
        packet.dataInt = new int[2];
        packet.dataInt[0] = entityId;
        packet.dataInt[1] = riddenByEntity != null ? riddenByEntity.entityId : 0;
        
        packet.dataFloat = new float[6];
        packet.dataFloat[0] = (float) posX;
        packet.dataFloat[1] = (float) posY;
        packet.dataFloat[2] = (float) posZ;
        packet.dataFloat[3] = rotationYaw;
        packet.dataFloat[4] = rotationPitch;
        packet.dataFloat[5] = rotationRoll;
        //packet.dataFloat[6] = (float) motionX;
        //packet.dataFloat[7] = (float) motionY;
        //packet.dataFloat[8] = (float) motionZ;
        
        return packet;
    }
    
    public void handleUpdatePacketFromClient(Packet230ModLoader packet)
    {
        plog("handleUpdatePacketFromClient: " + packet); // inbound packet not aligned with plog unless very high update rate
        
        latestUpdatePacket = packet;
    }
    
    private void applyUpdatePacketFromClient()
    {
        plog("applyUpdatePacketFromClient: " + latestUpdatePacket);
        
        if (latestUpdatePacket == null) return;
        
        Packet230ModLoader packet = latestUpdatePacket;
        latestUpdatePacket = null;
        
        //setPosition(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2]);
        //setRotation(packet.dataFloat[3], packet.dataFloat[4]);
        
        setPositionAndRotation(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2], packet.dataFloat[3], packet.dataFloat[4]);
        
        rotationRoll = packet.dataFloat[5] % 360f;

        // for now, clear any motion
        motionX = .0; // packet.dataFloat[6];
        motionY = .0; // packet.dataFloat[7];
        motionZ = .0; // packet.dataFloat[8];
        
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
    
    @Override
    protected void setBeenAttacked()
    {
        log("setBeenAttacked called");
        super.setBeenAttacked();
        //above will set velocityChanged = true which will send extra packet if on server
    }
}
