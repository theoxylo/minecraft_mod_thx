package net.minecraft.src;

import java.util.List;
import java.util.Random;

public abstract class ThxEntityRocketBase extends ThxEntityProjectile
{
	public ThxEntityRocketBase(World world)
    {
        super(world);
    }

    public ThxEntityRocketBase(Entity owner, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        super(owner, x, y, z, dx, dy, dz, yaw, pitch);
    }
    
    @Override
    public int getPacketTypeId()
    {
        return 76;
    }
    
    @Override
    void strikeEntity(Entity entity)
    {
        int attackStrength = 6;
        entity.attackEntityFrom(new EntityDamageSource("player", owner), attackStrength);
    }
    
    @Override
    void createParticles()
    {
        worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0, 0.0, 0.0);
    }
    
    @Override
    void onLaunch()
    {
        log("onLaunch");
        //worldObj.playSoundAtEntity(this, "random.fizz", 1f, 1f);
        worldObj.provider.worldObj.playSoundAtEntity(this, "random.fizz", 1f, 1f);
    }
    
    @Override
    float getAcceleration()
    {
        //return 1.2f;
        return 1.2f;
    }
    
    @Override
    void detonate()
    {
        doSplashDamage(1.0, 2); // very small splash damage
            
        // no explosion, just the sound
        worldObj.playSoundAtEntity(this, "random.explode", .7f, .5f + (float) Math.random() * .2f);
        
        setDead();
    }
}

