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
    
    public Packet230ModLoader getSpawnPacket()
    {
        Packet230ModLoader packet = getUpdatePacket();
        packet.dataString[0] = "thx spawn packet for entity " + entityId;
        
        
        // bump Y for non-piloted to avoid getting stuck in landscape
        if (riddenByEntity == null)
        {
            packet.dataFloat[1] += .2;
        }
        
        log("Created new spawn packet: " + packet);
        return packet;
    }
        
    public void handleUpdatePacketFromClient(Packet230ModLoader packet)
    {
        //log("handleUpdatePacketFromClient: " + packet); // inbound packet not aligned with plog unless very high update rate
        
        latestUpdatePacket = packet;
    }
    
    private void applyUpdatePacketFromClient()
    {
        //plog("applyUpdatePacketFromClient: " + latestUpdatePacket);
        
        if (latestUpdatePacket == null) return;
        
        Packet230ModLoader packet = latestUpdatePacket;
        latestUpdatePacket = null;
        
        // fire controls, used by server to spawn projectiles
        fire1 = packet.dataInt[2];
        fire2 = packet.dataInt[3];
        if (fire1 > 0 || fire2 > 0) log("entity is firing: " + fire1 + ", " + fire2);
        
        setPositionAndRotation(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2], packet.dataFloat[3], packet.dataFloat[4]);
        
        rotationRoll = packet.dataFloat[5] % 360f;

        // for now, clear any motion
        motionX =  packet.dataFloat[6];
        motionY =  packet.dataFloat[7];
        motionZ =  packet.dataFloat[8];
        
        damage = packet.dataFloat[9];
        throttle = packet.dataFloat[10];

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
