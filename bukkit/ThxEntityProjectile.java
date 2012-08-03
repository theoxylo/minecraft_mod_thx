package net.minecraft.server;

import java.util.List;

public abstract class ThxEntityProjectile extends ThxEntity
{
    boolean enteredWater;
    boolean launched;

    public ThxEntityProjectile(World var1)
    {
        super(var1);
        this.helper = this.createHelper();
        this.b(0.5F, 0.5F);
    }

    public ThxEntityProjectile(Entity var1, double var2, double var4, double var6, double var8, double var10, double var12, float var14, float var15)
    {
        this(var1.world);
        this.owner = var1;
        this.setLocation(var2, var4, var6, var14, var15);
        float var16 = this.getAcceleration();
        this.updateRotation();
        this.updateVectors();
        this.motX = (double)(this.fwd.x * var16) + var8;
        this.motY = (double)(this.fwd.y * var16) + var10;
        this.motZ = (double)(this.fwd.z * var16) + var12;
        this.isFireproof();
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void F_()
    {
        super.F_();

        if (this.owner == null)
        {
            this.die();
        }
        else if (this.ticksLived > 300)
        {
            this.detonate();
        }
        else
        {
            if (!this.launched)
            {
                this.launched = true;
                this.onLaunch();
            }

            this.createParticles();
            Vec3D var1 = Vec3D.create(this.locX, this.locY, this.locZ);
            Vec3D var2 = Vec3D.create(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
            MovingObjectPosition var3 = this.world.a(var1, var2);

            if (var3 != null)
            {
                var2 = Vec3D.create(var3.pos.a, var3.pos.b, var3.pos.c);
            }

            this.setPosition(var2.a, var2.b, var2.c);

            if (!this.enteredWater && this.aU())
            {
                this.enteredWater = true;
                this.motX *= 0.699999988079071D;
                this.motY *= 0.699999988079071D;
                this.motZ *= 0.699999988079071D;
                this.world.makeSound(this, "random.splash", 1.0F, 1.0F);

                for (int var4 = 0; var4 < 4; ++var4)
                {
                    float var5 = 0.25F;
                    this.world.a("bubble", this.locX - this.motX * (double)var5, this.locY - this.motY * (double)var5, this.locZ - this.motZ * (double)var5, this.motX, this.motY, this.motZ);
                }
            }

            Entity var15 = null;
            double var16 = 1.0D;
            List var7 = this.world.getEntities(this, this.boundingBox.grow(1.0D, 1.0D, 1.0D));
            int var8;

            for (var8 = 0; var8 < var7.size(); ++var8)
            {
                Entity var9 = (Entity)var7.get(var8);

                if (var9 != null && var9.o_() && !var9.equals(this.owner))
                {
                    if (this.owner == null)
                    {
                        this.log(this + " owner is null");
                    }

                    if (!this.owner.equals(var9.passenger) && !this.owner.equals(var9))
                    {
                        float var10 = 0.3F;
                        AxisAlignedBB var11 = var9.boundingBox.grow((double)var10, (double)var10, (double)var10);
                        MovingObjectPosition var12 = var11.a(var1, var2);

                        if (var12 != null)
                        {
                            double var13 = var1.b(var12.pos);

                            if (var13 < var16)
                            {
                                var15 = var9;
                                var16 = var13;
                            }
                        }
                    }
                    else
                    {
                        this.log("skipping self");
                    }
                }
            }

            if (var15 != null)
            {
                var3 = new MovingObjectPosition(var15);

                if (this.owner != null && !this.owner.equals(var15.passenger) && !this.owner.equals(var15))
                {
                    this.strikeEntity(var15);
                    this.detonate();
                    return;
                }

                this.log(this.owner + " ignoring hit from own rocket");
            }

            if (var3 != null)
            {
                var8 = MathHelper.floor(this.locX);
                int var17 = MathHelper.floor(this.locY - 0.2D - (double)this.height);
                int var19 = MathHelper.floor(this.locZ);
                int var18 = this.world.getTypeId(var8, var17, var19);
                int var20;

                if (var18 > 0)
                {
                    for (var20 = 0; var20 < 4; ++var20)
                    {
                        this.world.a("tilecrack_" + var18, this.locX + ((double)this.random.nextFloat() - 0.5D) * (double)this.width, this.boundingBox.b + 0.1D, this.locZ + ((double)this.random.nextFloat() - 0.5D) * (double)this.width, 1.0D + ((double)this.random.nextFloat() - 0.5D), 1.0D + ((double)this.random.nextFloat() - 0.5D), 1.0D + ((double)this.random.nextFloat() - 0.5D));
                    }
                }
                else
                {
                    for (var20 = 0; var20 < 4; ++var20)
                    {
                        this.world.a("snowballpoof", this.locX + ((double)this.random.nextFloat() - 0.5D), this.boundingBox.b + 0.1D, this.locZ + ((double)this.random.nextFloat() - 0.5D), 1.0D + ((double)this.random.nextFloat() - 0.5D), 1.0D + ((double)this.random.nextFloat() - 0.5D), 1.0D + ((double)this.random.nextFloat() - 0.5D));
                    }
                }

                this.detonate();
            }
        }
    }

    void doSplashDamage(double var1, int var3)
    {
        List var4 = this.world.getEntities(this, this.boundingBox.grow(var1, var1, var1));

        for (int var5 = 0; var5 < var4.size(); ++var5)
        {
            Entity var6 = (Entity)var4.get(var5);

            if (var6.o_())
            {
                var6.damageEntity(new EntityDamageSource("projectile splash damage", this.owner), var3);
            }
        }
    }

    abstract float getAcceleration();

    abstract void onLaunch();

    abstract void createParticles();

    abstract void strikeEntity(Entity var1);

    abstract void detonate();
}
