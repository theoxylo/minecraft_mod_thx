package net.minecraft.src;

import java.util.List;


public class ThxEntityHelicopter extends ThxEntityHelicopterBase implements IClientDriven, ISpawnable
{
    public ThxEntityHelicopter(World world)
    {
        super(world);
	    helper = new ThxEntityHelperServer(this);
    }

    public ThxEntityHelicopter(World world, double x, double y, double z, float yaw)
    {
        this(world);
        setPositionAndRotation(x, y + yOffset, z, yaw, 0f);
    }

    @Override
    public void onUpdatePilot()
    {
        if (riddenByEntity == null) return;
        
        if (riddenByEntity.isDead) riddenByEntity.mountEntity(this);
        
        if (cmd_reload > 0)
        {
            cmd_reload = 0;
            reload();
        }
        
        if (cmd_create_item > 0)
        {
            cmd_create_item = 0;
            //convertToItem();
        }
        
        if (cmd_exit > 0)
        {
            cmd_exit = 0;
            pilotExit();
        }
        
        if (cmd_create_map > 0)
        {
            cmd_create_map = 0;
            createMap();
        }
    }
    
    @Override
    void updateMotion(boolean flag)
    {
        moveEntity(motionX, motionY, motionZ);
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, int i)
    {
        if (!super.attackEntityFrom(damageSource, i)) return false; // no hit

        takeDamage((float) i * 3f);

        timeSinceAttacked = .5f; // sec delay before this entity can be attacked again

        //setBeenAttacked(); // this will cause Entity.velocityChanged to be true, so additional Packet28 to jump on hit

        return true; // the hit landed
    }

    @Override
    protected void pilotExit()
    {
        super.pilotExit();
        
        if (riddenByEntity == null) return;
        
        EntityPlayerMP pilot = (EntityPlayerMP) riddenByEntity;
        riddenByEntity.mountEntity(this); // riddenByEntity is now null
        
        Packet packet = new Packet39AttachEntity(pilot, null);
        List players = ModLoader.getMinecraftServerInstance().configManager.playerEntities;
        for (Object player : players)
        {
            if (player.equals(pilot)) continue; // already sent above by mountEntity call
            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
        }

        // place pilot to left of helicopter
        // (use fwd XZ perp to exit left: x = z, z = -x)
        double exitDist = 1.9;
        pilot.playerNetServerHandler.teleportTo(posX + fwd.z * exitDist, posY + pilot.getYOffset(), posZ - fwd.x * exitDist, rotationYaw, 0f);

    }
 
    /* from ISpawnable interface */
    public Packet230ModLoader getSpawnPacket()
    {
        return helper.getSpawnPacket();
    }
    
    @Override
    public void updateRiderPosition()
    {
        if (riddenByEntity == null) return;
        
        super.updateRiderPosition();
        
        /* broken! causes fixed pilot until teleport when exit, interesting rotation acceleration on view makes it hard to play even as rc drone
        if (getDistanceSqToEntity(riddenByEntity) > 4)
        {
            System.out.println("Teleporting pilot to helicopter location");
            
	        EntityPlayerMP pilot = (EntityPlayerMP) riddenByEntity;
	        pilot.playerNetServerHandler.teleportTo(posX, posY + riddenByEntity.getYOffset() + getMountedYOffset(), posZ, rotationYaw, 0f);
        }
        */
    }
}
