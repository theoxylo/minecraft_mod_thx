package net.minecraft.src;

public class ThxEntityMissile extends ThxEntityMissileBase implements ISpawnable
{
    public ThxEntityMissile(World world)
    {
        super(world);
    }

    public ThxEntityMissile(World world, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        super(world, x, y, z, dx, dy, dz, yaw, pitch);
    }

    @Override
    public void onUpdate()
    {
        if (!launched)
        {
            launched = true;
            
            //onLaunch();
	        String sfx = "mob.ghast.fireball";
	        log("onLaunch: playSound: " + sfx);
	        worldObj.playSoundAtEntity(this, sfx, 1f, 1f);
        }

        exhaustTimer -= deltaTime;
        if (exhaustTimer < 0f)
        {
            exhaustTimer = EXHAUST_DELAY;
            worldObj.spawnParticle("largesmoke", posX, posY, posZ, 0.0, 0.0, 0.0);
        }
        worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0, 0.0, 0.0);
        
        if (worldObj.isRemote)
        {
	        moveEntity(motionX, motionY, motionZ); // this will update posX, posY, posZ
            return;
        }
        
        super.onUpdate();
    }
    
    /* from ISpawnable interface */
    @Override
    public void spawn(Packet230ModLoader packet)
    {
        helper.spawn(packet);
    }
    
    @Override
    ThxEntityHelper createHelper()
    {
        return new ThxEntityHelperClient(this, new ThxModelMissile());
    }
}
