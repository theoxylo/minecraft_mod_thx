package net.minecraft.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ThxEntityHelicopterBase extends ThxEntity implements IClientDriven
{
    List followers;
    int rocketCount;
    float MAX_HEALTH;
    float MAX_ACCEL;
    float GRAVITY;
    float MAX_VELOCITY;
    float FRICTION;
    float MAX_PITCH;
    float PITCH_SPEED_DEG;
    float PITCH_RETURN;
    float MAX_ROLL;
    float ROLL_SPEED_DEG;
    float ROLL_RETURN;
    float THROTTLE_MIN;
    float THROTTLE_MAX;
    float THROTTLE_INC;
    float MOMENTUM;
    boolean altitudeLock;
    float altitudeLockToggleDelay;
    float hudModeToggleDelay;
    float lookPitchToggleDelay;
    boolean lookPitch;
    float lookPitchZeroLevel;
    float createMapDelay;
    float pilotExitDelay;
    float missileDelay;
    final float MISSILE_DELAY;
    float rocketDelay;
    final float ROCKET_DELAY;
    final int FULL_ROCKET_COUNT;
    float rocketReload;
    final float ROCKET_RELOAD_DELAY;
    float autoLevelDelay;
    Vector3 thrust;
    Vector3 velocity;
    public ThxEntityHelicopter targetHelicopter;
    public boolean isTargetHelicopterFriendly;
    public boolean isDroneArmed;
    Vector3 deltaPosToTarget;
    ThxEntityMissile lastMissileFired;

    public ThxEntityHelicopterBase(World var1)
    {
        super(var1);
        this.followers = new ArrayList();
        this.MAX_HEALTH = 160.0F;
        this.MAX_ACCEL = 0.2F;
        this.GRAVITY = 0.2005F;
        this.MAX_VELOCITY = 0.26F;
        this.FRICTION = 0.98F;
        this.MAX_PITCH = 50.0F;
        this.PITCH_SPEED_DEG = 40.0F;
        this.PITCH_RETURN = 0.98F;
        this.MAX_ROLL = 30.0F;
        this.ROLL_SPEED_DEG = 40.0F;
        this.ROLL_RETURN = 0.92F;
        this.THROTTLE_MIN = -0.06F;
        this.THROTTLE_MAX = 0.09F;
        this.THROTTLE_INC = 0.004F;
        this.MOMENTUM = 0.2F;
        this.lookPitch = false;
        this.MISSILE_DELAY = 6.0F;
        this.ROCKET_DELAY = 0.3F;
        this.FULL_ROCKET_COUNT = 12;
        this.ROCKET_RELOAD_DELAY = 3.0F;
        this.thrust = new Vector3();
        this.velocity = new Vector3();
        this.deltaPosToTarget = new Vector3();
        this.b(1.8F, 2.0F);
        this.height = 0.8F;
        this.helper = this.createHelper();
    }

    public ThxEntityHelicopterBase(World var1, double var2, double var4, double var6, float var8)
    {
        this(var1);
        this.setLocation(var2, var4 + (double)this.height, var6, var8, 0.0F);
    }

    public int getPacketTypeId()
    {
        return 75;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void F_()
    {
        super.F_();
        this.missileDelay -= this.deltaTime;
        this.rocketDelay -= this.deltaTime;

        if (this.rocketReload > 0.0F)
        {
            this.rocketReload -= this.deltaTime;
        }

        if (this.rocketReload < 0.0F)
        {
            this.rocketReload = 0.0F;
            this.world.makeSound(this, "random.click", 0.4F, 0.7F);
        }

        if (this.passenger != null)
        {
            this.onUpdatePilot();
            this.updateMotion(this.altitudeLock);
        }
        else if (this.targetHelicopter != null)
        {
            this.onUpdateDrone();
            this.updateMotion(false);
        }
        else
        {
            this.onUpdateVacant();
            this.updateMotion(false);
        }

        if (this.handleCollisions())
        {
            this.helper.addChatMessageToPilot("Damage: " + (int)(this.damage * 100.0F / this.MAX_HEALTH) + "%");
        }

        if (this.damage > this.MAX_HEALTH && !this.world.isStatic)
        {
            float var1 = 2.3F;
            boolean var2 = true;
            this.world.createExplosion(this, this.locX, this.locY, this.locZ, var1, var2);

            if (this.passenger != null)
            {
                this.pilotExit();
            }

            this.a(ThxItemHelicopter.shiftedId, 1, 0.0F);
            this.die();
        }

        if (this.isBurning())
        {
            this.damage += this.deltaTime * 5.0F;
        }
        else if (this.damage > 0.0F)
        {
            this.damage -= this.deltaTime;
        }

        if (this.damage / this.MAX_HEALTH > 0.9F && this.ticksLived % 20 == 0)
        {
            this.helper.addChatMessageToPilot("Damage: " + (int)(this.damage * 100.0F / this.MAX_HEALTH) + "%");
        }
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    public double x_()
    {
        return -0.25D;
    }

    void fireRocket()
    {
        if (this.rocketDelay <= 0.0F || this.passenger != null)
        {
            if (this.rocketReload <= 0.0F)
            {
                this.rocketDelay = 0.3F;
                ++this.rocketCount;
                float var1 = 0.6F;
                float var2 = this.rocketCount % 2 == 0 ? var1 : -var1;
                float var3 = this.side.x * var2 + this.fwd.x * 2.5F + this.up.x * -0.5F;
                float var4 = this.side.y * var2 + this.fwd.y * 2.5F + this.up.y * -0.5F;
                float var5 = this.side.z * var2 + this.fwd.z * 2.5F + this.up.z * -0.5F;
                float var6 = this.yaw;
                float var7 = this.pitch + 5.0F;

                if (!this.world.isStatic)
                {
                    Object var8 = this.passenger != null ? this.passenger : this;
                    ThxEntityRocket var9 = new ThxEntityRocket((Entity)var8, this.locX + (double)var3, this.locY + (double)var4, this.locZ + (double)var5, this.motX * (double)this.MOMENTUM, this.motY * (double)this.MOMENTUM, this.motZ * (double)this.MOMENTUM, var6, var7);
                    this.world.addEntity(var9);
                    Iterator var10 = this.followers.iterator();

                    while (var10.hasNext())
                    {
                        Object var11 = var10.next();
                        ThxEntityHelicopter var12 = (ThxEntityHelicopter)var11;

                        if (var12.isDroneArmed)
                        {
                            var12.fireRocket();
                        }
                    }
                }

                if (this.rocketCount == 12)
                {
                    this.reload();
                }
            }
        }
    }

    void reload()
    {
        this.rocketReload = 3.0F;
        this.rocketCount = 0;
    }

    void fireMissile()
    {
        if (this.missileDelay > 0.0F)
        {
            if (this.lastMissileFired != null && !this.lastMissileFired.dead)
            {
                this.log("remote detonating missile");
                this.lastMissileFired.detonate();
            }
        }
        else
        {
            this.log("firing missile");
            this.missileDelay = 6.0F;
            float var1 = this.fwd.x * 2.5F + this.up.x * -0.5F;
            float var2 = this.fwd.y * 2.5F + this.up.y * -0.5F;
            float var3 = this.fwd.z * 2.5F + this.up.z * -0.5F;
            float var4 = this.yaw;
            float var5 = this.pitch + 5.0F;

            if (!this.world.isStatic)
            {
                Object var6 = this.passenger != null ? this.passenger : this;
                ThxEntityMissile var7 = new ThxEntityMissile((Entity)var6, this.locX + (double)var1, this.locY + (double)var2, this.locZ + (double)var3, this.motX * (double)this.MOMENTUM, this.motY * (double)this.MOMENTUM, this.motZ * (double)this.MOMENTUM, var4, var5);
                this.lastMissileFired = var7;
                this.world.addEntity(var7);
                Object var9;
                ThxEntityHelicopter var10;

                for (Iterator var8 = this.followers.iterator(); var8.hasNext(); var10 = (ThxEntityHelicopter)var9)
                {
                    var9 = var8.next();
                }
            }
        }
    }

    void createMap()
    {
        this.log("creating map");
        short var1 = 960;
        int var2 = (int)this.locX / var1;

        if (this.locX < 0.0D)
        {
            --var2;
        }
        else
        {
            ++var2;
        }

        int var3 = (int)this.locZ / var1;

        if (this.locZ < 0.0D)
        {
            --var3;
        }
        else
        {
            ++var3;
        }

        int var4 = (var2 + 50) * 100 + var3 + 50;
        ItemStack var5 = new ItemStack(Item.MAP.id, 1, var4);
        String var6 = "map_" + var4;
        WorldMap var7 = (WorldMap)this.world.a(WorldMap.class, var6);

        if (var7 == null)
        {
            var7 = new WorldMap(var6);
            this.world.a(var6, var7);
            int var8 = var2 * var1;

            if (var8 < 0)
            {
                var8 += var1 / 2;
            }
            else
            {
                var8 -= var1 / 2;
            }

            var7.centerX = var8;
            int var9 = var3 * var1;

            if (var9 < 0)
            {
                var9 += var1 / 2;
            }
            else
            {
                var9 -= var1 / 2;
            }

            var7.centerZ = var9;
            var7.scale = 3;
            var7.map = (byte)this.world.worldProvider.dimension;
            var7.a();
        }

        this.a(var5, 0.5F);
    }

    void updateMotion(boolean var1)
    {
        this.thrust.y = MathHelper.cos(this.pitchRad) * MathHelper.cos(this.rollRad) * MathHelper.cos(this.rollRad);
        float var2 = 1.0F - MathHelper.cos(this.pitchRad);

        if (this.pitchRad > 0.0F)
        {
            var2 *= -1.0F;
        }

        this.thrust.x = -this.fwd.x * var2;
        this.thrust.z = -this.fwd.z * var2;
        this.thrust.y += -this.fwd.y * var2;
        var2 = 1.0F - MathHelper.cos(this.rollRad);

        if (this.rollRad > 0.0F)
        {
            var2 *= -1.0F;
        }

        this.thrust.x -= this.fwd.z * var2;
        this.thrust.z += this.fwd.x * var2;
        this.velocity.set((float)this.motX, (float)this.motY, (float)this.motZ);
        this.velocity.scale(this.FRICTION);
        this.thrust.normalize().scale(this.MAX_ACCEL * (1.0F + this.throttle) * this.deltaTime / 0.05F);
        Vector3.add(this.velocity, this.thrust, this.velocity);
        this.velocity.y -= this.GRAVITY * this.deltaTime / 0.05F;

        if (this.velocity.lengthSquared() > this.MAX_VELOCITY * this.MAX_VELOCITY)
        {
            this.velocity.scale(this.MAX_VELOCITY / this.velocity.length());
        }

        this.motX = (double)this.velocity.x;
        this.motY = (double)this.velocity.y;
        this.motZ = (double)this.velocity.z;

        if (var1 && this.passenger != null)
        {
            this.motY *= 0.9D;

            if (this.motY < 1.0E-5D)
            {
                this.motY = 0.0D;
            }
        }

        this.move(this.motX, this.motY, this.motZ);
    }

    boolean handleCollisions()
    {
        boolean var1 = false;

        if (this.positionChanged || this.bz || var1)
        {
            double var2 = this.motX * this.motX + this.motY * this.motY + this.motZ * this.motZ;

            if (var2 > 0.005D && this.timeSinceCollided < 0.0F && !this.onGround)
            {
                this.log("crash velSq: " + var2);
                this.timeSinceCollided = 0.2F;
                float var4;

                if (this.passenger != null)
                {
                    var4 = (float)var2 * 1000.0F;

                    if (var4 < 3.0F)
                    {
                        var4 = 3.0F;
                    }

                    if (var4 > 49.0F)
                    {
                        var4 = 49.0F;
                    }

                    this.log("crash damage: " + var4);
                    this.takeDamage(var4);
                }

                for (int var6 = 0; var6 < 5; ++var6)
                {
                    this.world.a("explode", this.locX - 1.0D + Math.random() * 2.0D, this.locY - 1.0D + Math.random() * 2.0D, this.locZ - 1.0D + Math.random() * 2.0D, 0.0D, 0.0D, 0.0D);
                }

                var4 = (float)var2 * 10.0F;

                if (var4 > 0.8F)
                {
                    var4 = 0.8F;
                }

                if (var4 < 0.3F)
                {
                    var4 = 0.3F;
                }

                float var5 = 0.4F + this.world.random.nextFloat() * 0.4F;
                this.log("volume: " + var4 + ", pitch: " + var5);
                this.world.makeSound(this, "random.explode", var4, var5);
                this.motX *= 0.7D;
                this.motY *= 0.7D;
                this.motZ *= 0.7D;
                return true;
            }
        }

        return false;
    }

    public void i_()
    {
        if (this.passenger != null)
        {
            this.lastYaw = this.yaw;
            this.lastPitch = this.pitch;
            this.passenger.setPosition(this.locX, this.locY + this.passenger.W() + this.x_(), this.locZ);
        }
    }

    void attackedByPilot()
    {
        this.fireMissile();
    }

    void interactByPilot()
    {
        this.fireRocket();
    }

    void convertToItem()
    {
        this.pilotExit();
        this.die();

        if (this instanceof ThxEntityHelicopter && !this.world.isStatic)
        {
            this.a(ThxItemHelicopter.shiftedId, 1, 0.0F);
        }
    }

    abstract void onUpdatePilot();

    void onUpdateDrone()
    {
        if (this.targetHelicopter != null)
        {
            if (this.targetHelicopter.dead)
            {
                this.targetHelicopter = null;
            }
            else if (!this.world.isStatic)
            {
                float var1 = 0.0F;
                this.deltaPosToTarget.set((float)(this.targetHelicopter.locX - this.locX), 0.0F, (float)(this.targetHelicopter.locZ - this.locZ));
                var1 = this.deltaPosToTarget.length();

                if (!this.isTargetHelicopterFriendly && var1 < 20.0F && var1 > 5.0F && Math.abs(this.targetHelicopter.locY - this.locY) < 2.0D)
                {
                    if ((double)this.damage > 0.6D * (double)this.MAX_HEALTH && this.missileDelay < 0.0F)
                    {
                        this.fireMissile();
                    }
                    else if (this.rocketDelay < 0.0F)
                    {
                        this.fireRocket();
                    }
                }

                if (this.isTargetHelicopterFriendly && var1 < 10.0F)
                {
                    float var3;

                    for (var3 = this.targetHelicopter.yaw - this.yaw; var3 > 180.0F; var3 -= 360.0F)
                    {
                        ;
                    }

                    while (var3 < -180.0F)
                    {
                        var3 += 360.0F;
                    }

                    this.rotationYawSpeed = var3 * 3.0F;

                    if (this.rotationYawSpeed > 90.0F)
                    {
                        this.rotationYawSpeed = 90.0F;
                    }

                    if (this.rotationYawSpeed < -90.0F)
                    {
                        this.rotationYawSpeed = -90.0F;
                    }

                    this.yaw += this.rotationYawSpeed * this.deltaTime;
                }
                else
                {
                    Vector3 var2 = Vector3.add(this.targetHelicopter.pos, this.pos.negate((Vector3)null), (Vector3)null);

                    if (Vector3.dot(this.side, var2) > 0.0F)
                    {
                        this.yaw += 60.0F * this.deltaTime;
                    }
                    else
                    {
                        this.yaw -= 60.0F * this.deltaTime;
                    }
                }

                if (var1 > 10.0F)
                {
                    this.pitch = 45.0F * (var1 - 10.0F) / 20.0F;
                }
                else if (!this.isTargetHelicopterFriendly)
                {
                    this.pitch = (1.0F - var1 / 10.0F) * -20.0F;
                }

                this.rotationPitchSpeed = 0.0F;

                if (this.isTargetHelicopterFriendly && var1 < 10.0F)
                {
                    this.pitch = this.targetHelicopter.pitch;
                    this.rotationPitchSpeed = this.targetHelicopter.rotationPitchSpeed;
                    this.rotationRoll = this.targetHelicopter.rotationRoll;
                    this.rotationRollSpeed = this.targetHelicopter.rotationRollSpeed;
                }

                if (this.locY + 1.0D < this.targetHelicopter.locY)
                {
                    if (this.throttle < this.THROTTLE_MAX * 0.6F)
                    {
                        this.throttle += this.THROTTLE_INC * 0.4F;
                    }

                    if (this.throttle > this.THROTTLE_MAX * 0.6F)
                    {
                        this.throttle = this.THROTTLE_MAX * 0.6F;
                    }
                }
                else if (this.locY - 2.0D > this.targetHelicopter.locY)
                {
                    if (this.throttle > this.THROTTLE_MIN * 0.6F)
                    {
                        this.throttle -= this.THROTTLE_INC * 0.4F;
                    }

                    if (this.throttle < this.THROTTLE_MIN * 0.6F)
                    {
                        this.throttle = this.THROTTLE_MIN * 0.6F;
                    }
                }
                else
                {
                    this.throttle = (float)((double)this.throttle * 0.6D);
                }
            }
        }
    }

    void onUpdateVacant()
    {
        this.isActive = false;
        this.throttle = (float)((double)this.throttle * 0.8D);

        if (this.onGround)
        {
            if (Math.abs(this.pitch) > 0.1F)
            {
                this.pitch *= 0.7F;
            }

            if (Math.abs(this.rotationRoll) > 0.1F)
            {
                this.rotationRoll *= 0.7F;
            }

            this.motX *= (double)this.FRICTION;
            this.motX *= (double)this.FRICTION;
            this.motY = 0.0D;
            this.motZ *= (double)this.FRICTION;
            this.motZ *= (double)this.FRICTION;
            this.rotationYawSpeed = 0.0F;
        }
        else if (this.bV)
        {
            if (Math.abs(this.pitch) > 0.1F)
            {
                this.pitch *= 0.7F;
            }

            if (Math.abs(this.rotationRoll) > 0.1F)
            {
                this.rotationRoll *= 0.7F;
            }

            this.motX *= 0.7D;
            this.motY *= 0.7D;
            this.motZ *= 0.7D;
            this.motY += 0.01D;
        }
        else
        {
            this.pitch *= this.PITCH_RETURN;
            this.rotationRoll *= this.ROLL_RETURN;
            this.motX *= (double)this.FRICTION;
            this.motY -= (double)(this.GRAVITY * this.deltaTime);
            this.motZ *= (double)this.FRICTION;
        }
    }

    void attackedByThxEntity(ThxEntity var1)
    {
        if (this.world.isStatic)
        {
            ;
        }

        if (this.passenger != null)
        {
            ;
        }

        if (this.passenger == null && var1 instanceof ThxEntityHelicopter)
        {
            this.log("attacked by " + var1 + " with pilot: " + var1.passenger);

            if (this.targetHelicopter == null)
            {
                this.targetHelicopter = (ThxEntityHelicopter)var1;
                this.targetHelicopter.followers.add(this);
                this.isTargetHelicopterFriendly = true;
                this.isDroneArmed = false;
                this.world.makeSound(this, "random.fuse", 1.0F, 1.0F);
                this.log("new targetHelicopter: " + this.targetHelicopter);
            }
            else if (this.targetHelicopter.equals(var1))
            {
                if (this.isTargetHelicopterFriendly)
                {
                    if (!this.isDroneArmed)
                    {
                        this.isDroneArmed = true;
                        this.owner = this.targetHelicopter.owner;
                        this.world.makeSound(this, "random.fuse", 1.0F, 1.0F);
                    }
                    else
                    {
                        this.isTargetHelicopterFriendly = false;
                        this.targetHelicopter.followers.remove(this);
                        this.owner = this;
                        this.missileDelay = 10.0F;
                        this.rocketDelay = 5.0F;
                        this.world.makeSound(this, "random.fuse", 1.0F, 1.0F);
                    }
                }
                else if (this.damage / this.MAX_HEALTH > 0.9F && !this.isBurning())
                {
                    this.isTargetHelicopterFriendly = true;
                    this.isDroneArmed = false;
                }
            }
            else
            {
                this.targetHelicopter.followers.remove(this);
                this.targetHelicopter = (ThxEntityHelicopter)var1;
                this.isTargetHelicopterFriendly = false;
                this.world.makeSound(this, "random.fuse", 1.0F, 1.0F);
                this.log("new enemy targetHelicopter: " + this.targetHelicopter);
            }
        }
    }

    void pilotExit()
    {
        this.log("pilotExit called for pilot entity " + (this.passenger != null ? this.passenger.id : 0));
        this.targetHelicopter = null;

        if (!this.world.isStatic)
        {
            if (this.passenger != null)
            {
                if (this.equals(this.passenger.vehicle))
                {
                    this.passenger.vehicle = null;
                    this.passenger = null;
                    this.targetHelicopter = null;
                    this.rotationYawSpeed = 0.0F;
                    this.rotationPitchSpeed = 0.0F;
                    this.rotationRollSpeed = 0.0F;
                }
            }
        }
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean b(EntityHuman var1)
    {
        if (!super.b(var1))
        {
            return false;
        }
        else
        {
            this.targetHelicopter = null;
            return true;
        }
    }
}
