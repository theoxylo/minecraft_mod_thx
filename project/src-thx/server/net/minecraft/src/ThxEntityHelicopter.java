package net.minecraft.src;

import java.util.List;


public class ThxEntityHelicopter extends ThxEntityHelicopterBase implements IClientDriven, ISpawnable
{
    public ThxEntityHelicopter(World world)
    {
        super(world);
        
	    helper = new ThxEntityHelperServer(this);
	    
        setSize(1.8f, 2f);
        yOffset = .6f;
        NET_PACKET_TYPE = 75;
        log("C1 - ThxEntityHelicopter() with world: " + world.getWorldInfo());
    }

    public ThxEntityHelicopter(World world, double x, double y, double z, float yaw)
    {
        this(world);
        
        setPositionAndRotation(x, y + yOffset, z, yaw, 0f);
        log("C2 - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ + ", yaw: " + yaw);
    }

    @Override
    public void onUpdate()
    {
        helper.applyUpdatePacketFromClient();    
        
        super.onUpdate();
        
        if (riddenByEntity != null)
        {
            // entity updates will come from client for player pilot
            
            if (riddenByEntity.isDead) riddenByEntity.mountEntity(this);
	        
	        // fire weapons and clear flags
            if (fire1 > 0)
            {
                fire1 = 0;
                fireRocket();
            }
            if (fire2 > 0)
            {
                fire2 = 0;
                fireMissile();
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
        else
        {
	        // for auto-heal unattended, otherwise damage set by pilot client
	        if (damage > 0f) damage -= deltaTime; // heal rate: 1 pt / sec
	
	        onUpdateVacant();
        }
            
        moveEntity(motionX, motionY, motionZ);
        
        handleCollisions();
    }
    
    protected void onUpdateVacant()
    {
        // adjust position height to avoid collisions
        List list = worldObj.getCollidingBoundingBoxes(this, boundingBox.contract(0.03125, 0.0, 0.03125));
        if (list.size() > 0)
        {
            double d3 = 0.0D;
            for (int j = 0; j < list.size(); j++)
            {
                AxisAlignedBB axisalignedbb = (AxisAlignedBB)list.get(j);
                if (axisalignedbb.maxY > d3)
                {
                    d3 = axisalignedbb.maxY;
                }
            }

            posY += d3 - boundingBox.minY;
            setPosition(posX, posY, posZ);
        }

        if (throttle > .001) log("throttle: " + throttle);
        
        throttle *= .6; // quickly zero throttle
        
        if (onGround || inWater)
        {
            if (Math.abs(rotationPitch) > .1f) rotationPitch *= .70f;
            if (Math.abs(rotationRoll) > .1f) rotationRoll *= .70f; // very little lateral
                
            // tend to stay put on ground
            motionY = 0.;
            motionX *= .7;
            motionZ *= .7;
                
            rotationYawSpeed = 0f;
        }
        else
        {
            // settle back to ground naturally if pilot bails
                
            rotationPitch *= PITCH_RETURN;
            rotationRoll *= ROLL_RETURN;
                
            motionX *= FRICTION;
            motionY -= GRAVITY * .16f * deltaTime / .05f;
            motionZ *= FRICTION;
        }
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
}
