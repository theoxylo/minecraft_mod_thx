package net.minecraft.server;

public abstract class ThxEntityRocketBase extends ThxEntityProjectile
{
    public ThxEntityRocketBase(World var1)
    {
        super(var1);
    }

    public ThxEntityRocketBase(Entity var1, double var2, double var4, double var6, double var8, double var10, double var12, float var14, float var15)
    {
        super(var1, var2, var4, var6, var8, var10, var12, var14, var15);
    }

    public int getPacketTypeId()
    {
        return 76;
    }

    void strikeEntity(Entity var1)
    {
        byte var2 = 6;
        var1.damageEntity(new EntityDamageSource("player", this.owner), var2);
    }

    void createParticles()
    {
        this.world.a("smoke", this.locX, this.locY, this.locZ, 0.0D, 0.0D, 0.0D);
    }

    void onLaunch()
    {
        this.world.makeSound(this, "random.fizz", 1.0F, 1.0F);
    }

    float getAcceleration()
    {
        return 1.2F;
    }

    void detonate()
    {
        this.doSplashDamage(1.0D, 2);
        this.world.makeSound(this, "random.explode", 0.7F, 0.5F + (float)Math.random() * 0.2F);
        this.die();
    }
}
