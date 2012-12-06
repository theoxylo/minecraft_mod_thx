package net.minecraft.src;

public class ThxEntityHelperServer extends ThxEntityHelper
{
    public ThxEntityHelperServer(ThxEntity entity)
    {
        this.entity = entity;
    }
    
    // needed?
    void applyUpdatePacket(ThxEntityPacket250 packet)
    {
        if (true) return;
        
        if (packet == null) return;
        
        if (entity.riddenByEntity == null)
        {
            System.out.println("update received even though no pilot");
        }
        else if (entity.riddenByEntity.isDead)
        {
	        // last update for this player packet if player is dead
            log ("pilot entity is dead");
            //entity.riddenByEntity.mountEntity(entity);
            entity.pilotExit();
        }
        else 
        {
            entity.updateRiderPosition();
        }
    }

    @Override
    void addChatMessageToAll(String s)
    {
        log("Chat broadcast: " + s);
    }
}
