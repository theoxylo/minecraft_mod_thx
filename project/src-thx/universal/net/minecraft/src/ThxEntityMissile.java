package net.minecraft.src;

import java.util.List;
import java.util.Random;

public class ThxEntityMissile extends ThxEntityProjectile
{
	public ThxEntityMissile(World world)
    {
        super(world);
    }

    public ThxEntityMissile(Entity owner, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        super(owner, x, y, z, dx, dy, dz, yaw, pitch);
    }
    
    @Override
    ThxEntityHelper createHelper()
    {
        return new ThxEntityHelperClient(this, new ThxModelMissile());
    }
    
    @Override
    void createParticles()
    {
        worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0, 0.0, 0.0);
        worldObj.spawnParticle("largesmoke", posX, posY, posZ, 0.0, 0.0, 0.0);
    }
    
    @Override
    void onLaunch()
    {
        System.out.println("Missile launch: " + this);
        worldObj.playSoundAtEntity(this, "mob.irongolem.throw", 1f, 1f);
    }
    
    @Override
    float getAcceleration()
    {
        return .52f;
    }
    
    @Override
    void strikeEntity(Entity entity)
    {
        int attackStrength = 12; // about 2 hearts for other player without armor??;
        entity.attackEntityFrom(new EntityDamageSource("player", owner), attackStrength);
        
        entity.setFire(10); // direct hit, burn for 10 seconds
    }
    
    @Override
    void detonate()
    {
        doSplashDamage(3.0, 6); // 1 heart if within range, see explosion impl for more precise way
            
        float power = 1.1f;
        boolean withFire = false;
        boolean smoking = false;
        worldObj.newExplosion(this, posX, posY, posZ, power, withFire, smoking);
        
        setDead();
    }
}

