package net.minecraft.src;

public class ThxEntityHelperServer extends ThxEntityHelper
{
    public ThxEntityHelperServer(ThxEntity entity)
    {
        this.entity = entity;
    }
    
    Packet230ModLoader getSpawnPacket()
    {
        Packet230ModLoader packet = entity.getUpdatePacket();
        packet.dataString[0] = "thx spawn packet for entity " + entity.entityId;
        
        // bump Y for non-piloted to avoid getting stuck in landscape
        if (entity.riddenByEntity == null)
        {
            packet.dataFloat[1] += .2;
        }
        
        log("Created new spawn packet: " + packet);
        return packet;
    }
        
    void applyUpdatePacketFromClient()
    {
        //plog("applyUpdatePacketFromClient: " + latestUpdatePacket);
        
        if (entity.latestUpdatePacket == null) return;
        
        Packet230ModLoader packet = entity.latestUpdatePacket;
        entity.latestUpdatePacket = null;
        
        // fire controls, used by server to spawn projectiles
        entity.fire1 = packet.dataInt[2];
        entity.fire2 = packet.dataInt[3];
        if (entity.fire1 > 0 || entity.fire2 > 0) log("entity is firing: " + entity.fire1 + ", " + entity.fire2);
        
        entity.setPositionAndRotation(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2], packet.dataFloat[3], packet.dataFloat[4]);
        
        entity.rotationRoll = packet.dataFloat[5] % 360f;

        // for now, clear any motion
        entity.motionX =  packet.dataFloat[6];
        entity.motionY =  packet.dataFloat[7];
        entity.motionZ =  packet.dataFloat[8];
        
        entity.damage = packet.dataFloat[9];
        entity.throttle = packet.dataFloat[10];

        if (entity.riddenByEntity == null)
        {
            System.out.println("update received even though no pilot");
        }
        else if (entity.riddenByEntity.isDead)
        {
	        // last update for this player packet if player is dead
            log ("pilot entity is dead");
            entity.riddenByEntity.mountEntity(entity);
        }
        else 
        {
            entity.updateRiderPosition();
        }
    }
}
