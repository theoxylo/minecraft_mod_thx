package net.minecraft.src;

import java.util.List;


public class ThxEntityHelicopter extends ThxEntityHelicopterBase implements ISpawnable
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
        int riddenById = riddenByEntity != null ? riddenByEntity.entityId : 0;
        plog(String.format("onUpdatePilot, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", riddenById, posX, posY, posZ, rotationYaw, throttle, motionX, motionY, motionZ));
        
        if (cmd_exit > 0 || riddenByEntity.isDead)
        {
            cmd_exit = 0;
            pilotExit();
        }
        
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
        
        if (cmd_create_map > 0)
        {
            cmd_create_map = 0;
            createMap();
        }
    }
    
    @Override
    void updateMotion(boolean flag)
    {
        if (riddenByEntity != null) // server motionX,Y,Z are updated by pilot client packet so just move
    	{
        	moveEntity(motionX, motionY, motionZ);
    	}
        else
        {
        	super.updateMotion(flag);
        }
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, int i)
    {
        if (!super.attackEntityFrom(damageSource, i)) return false; // no hit

        takeDamage((float) i * 3f);

        //setBeenAttacked(); // this will cause Entity.velocityChanged to be true, so additional Packet28 to jump on hit

        return true; // the hit landed
    }

    @Override
    void pilotExit()
    {
        if (riddenByEntity == null) return;
        
        Packet packet = new Packet39AttachEntity(riddenByEntity, null);
        List players = ModLoader.getMinecraftServerInstance().configManager.playerEntities;
        for (Object player : players)
        {
            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
        }

        // place pilot to left of helicopter
        // (use fwd XZ perp to exit left: x = z, z = -x)
        double exitDist = 1.9;
        ((EntityPlayerMP) riddenByEntity).playerNetServerHandler.teleportTo(posX + fwd.z * exitDist, posY + riddenByEntity.getYOffset(), posZ - fwd.x * exitDist, rotationYaw, 0f);

        super.pilotExit();
    }
}
