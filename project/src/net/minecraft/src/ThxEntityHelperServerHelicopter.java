package net.minecraft.src;

public class ThxEntityHelperServerHelicopter extends ThxEntityHelperServer
{
    ThxEntityHelicopter entity;

    ThxEntityHelperServerHelicopter(ThxEntityHelicopter e)
    {
        super(e);
        entity = e;
    }
    
    void pilotExit(Entity pilot)
    {
        entity.log("server helper pilotExit() called");
        
        System.out.println("*** ThxEntityHelperServerHelicopter.pilotExit()");
        // place pilot to left of helicopter
        // (use fwd XZ perp to exit left: x = z, z = -x)
        double exitDist = 1.9;
        ((EntityPlayerMP) pilot).playerNetServerHandler.setPlayerLocation(entity.posX + entity.fwd.z * exitDist, entity.posY + pilot.yOffset, entity.posZ - entity.fwd.x * exitDist, entity.rotationYaw, entity.rotationPitch);
            
        // causes mountEntity to be called on client
        //((EntityPlayerMP) pilot).playerNetServerHandler.sendPacketToPlayer(new Packet39AttachEntity(this, this.ridingEntity));
            
        /*
        Packet packet = new Packet39AttachEntity(pilot, null);
        ((EntityPlayerMP) pilot).playerNetServerHandler.sendPacketToPlayer(packet);
        ((EntityPlayerMP) pilot).playerNetServerHandler.setPlayerLocation(posX + fwd.z * exitDist, posY + pilot.yOffset, posZ - fwd.x * exitDist, this.rotationYaw, this.rotationPitch);
        */
            
        /* notify all players...
        List players = ModLoader.getMinecraftServerInstance().configManager.playerEntities;
        for (Object player : players)
        {
            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
        }

        double exitDist = 1.9;
        ((EntityPlayerMP) pilot).playerNetServerHandler.teleportTo(posX + fwd.z * exitDist, posY + riddenByEntity.getYOffset(), posZ - fwd.x * exitDist, rotationYaw, 0f);
        */
    }
    
    @Override
    void onUpdateWithPilot()
    {
        entity.moveEntity(entity.motionX, entity.motionY, entity.motionZ);
        
        if (entity.riddenByEntity.isDead)
        {
            entity.pilotExit();
        }
        
        if (entity.cmd_reload > 0)
        {
            entity.cmd_reload = 0;
            entity.reload();
        }
        
        if (entity.cmd_create_item > 0)
        {
            entity.cmd_create_item = 0;
            //entity.convertToItem();
        }
        
        if (entity.cmd_create_map > 0)
        {
            entity.cmd_create_map = 0;
            entity.createMap();
        }
    }
}
