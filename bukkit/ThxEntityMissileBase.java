package net.minecraft.server;

public abstract class ThxEntityMissileBase extends ThxEntityProjectile
{
    public ThxEntityMissileBase(World var1)
    {
        super(var1);
    }

    public ThxEntityMissileBase(Entity var1, double var2, double var4, double var6, double var8, double var10, double var12, float var14, float var15)
    {
        super(var1, var2, var4, var6, var8, var10, var12, var14, var15);
    }

    public int getPacketTypeId()
    {
        return 77;
    }

    void createParticles()
    {
        this.world.a("smoke", this.locX, this.locY, this.locZ, 0.0D, 0.0D, 0.0D);
        this.world.a("largesmoke", this.locX, this.locY, this.locZ, 0.0D, 0.0D, 0.0D);
    }

    void onLaunch()
    {
        this.world.makeSound(this, "mob.ghast.fireball", 1.0F, 1.0F);
    }

    float getAcceleration()
    {
        return 0.52F;
    }

    void strikeEntity(Entity var1)
    {
        byte var2 = 12;
        var1.damageEntity(new EntityDamageSource("player", this.owner), var2);
        var1.setOnFire(10);
    }

    void detonate()
    {
        this.doSplashDamage(3.0D, 6);
        float var1 = 1.1F;
        boolean var2 = false;
        this.world.createExplosion(this, this.locX, this.locY, this.locZ, var1, var2);
        this.die();
    }
}
