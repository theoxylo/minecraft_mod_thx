package net.minecraft.src;

public class ThxEntityMissile extends ThxEntity
{
    static int instanceCount = 0;

    final int MISSILE_RECHARGE = 90;
    final double MISSILE_ACCEL = .40;
    final double MAX_VELOCITY = .50;
    final double GRAVITY = .005;

    public ThxEntityMissile(World world)
    {
        super(world);
        
        model = new ThxModelMissile();
        renderTexture = "/thx/missile.png";

        setSize(0.25f, 0.25f);

        instanceCount++;
        log("Created ThxEntityMissile instance: " + instanceCount);
        
        log(toString() + " - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }

    public ThxEntityMissile(World world, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        this(world);

        log("launching missile");

        float yawRad = yaw * RAD_PER_DEG;
        float pitchRad = pitch * RAD_PER_DEG;

        float f1 = MathHelper.cos(-yawRad - PI);
        float f3 = MathHelper.sin(-yawRad - PI);
        float f5 = -MathHelper.cos(-pitchRad);
        float f7 = MathHelper.sin(-pitchRad);
        /*
         * Vec3D thrust = Vec3D.createVector(f3 * f5, f7, f1 * f5);
         * log("Missile thrust: " + thrust + ", speed: " +
         * thrust.lengthVector());
         */
        prevMotionX = motionX = f3 * f5 * MISSILE_ACCEL + dx;
        prevMotionY = motionY = f7 * MISSILE_ACCEL + dy;
        prevMotionZ = motionZ = f1 * f5 * MISSILE_ACCEL + dz;

        // set initial position out a bit
        // set previous pos to detect when stopped
        double start = 5.0;
        setPosition(x + motionX * start, y + motionY * start, z + motionZ * start);
        setRotation(yaw - 90f, pitch); // in degrees

        log("posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        worldObj.playSoundAtEntity(this, "mob.ghast.fireball", 1f, 1f);
        //worldObj.playSoundAtEntity(this, "random.fuse", 1f, 1f);
    }

    @Override
    protected void entityInit()
    {
        // log("EntityThxMissile entityInit called");
    }

    @Override
    public void onUpdate()
    {
        plog("onUpdate called");
        
        super.onUpdate();

        // start by moving entity
        // double speedSq = motionX*motionX + motionY*motionY + motionZ*motionZ;
        // if (speedSq > .05) moveEntity(motionX, motionY, motionZ);
        moveEntity(motionX, motionY, motionZ);

        double dx = posX - prevPosX;
        double dy = posY - prevPosY;
        double dz = posZ - prevPosZ;
        // double deltaPosSq = dx*dx + dy*dy + dz*dz;
        double deltaPosSqXZ = dx * dx + dz * dz;

        // detonate if we hit an obstacle: horizontal (XZ)
        // and vertical (Y) motion is blocked
        if (deltaPosSqXZ < .10 && dy * dy < .05)
        {
            worldObj.newExplosion(this, posX, posY, posZ, 1.0F, true);
            setEntityDead();

            prevMotionX = motionX = .0;
            prevMotionY = motionY = .0;
            prevMotionZ = motionZ = .0;

            // System.out.println("missile has stopped, deltaPosSQ: " + deltaPosSqXZ + ", deltaY: " + dy);
        }

        // gradual constant pitch down until 20 deg
        //if (rotationPitch < 20f) rotationPitch += .4f;
        
        // or calculated pitch from velocity using sqrt
        float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        //prevRotationYaw = rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
        rotationPitch = (float)((Math.atan2(motionY, f) * 180D) / 3.1415927410125732D);

        // spiral
        rotationRoll += 9f;

        // gravity pull
        if (motionY > -.09) motionY -= GRAVITY;

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;
        prevRotationRoll = rotationRoll;

        prevMotionX = motionX;
        prevMotionY = motionX;
        prevMotionZ = motionZ;
    }

    @Override
    public String toString()
    {
        return "Missile " + entityId;
    }

    @Override
    public void finalize()
    {
        System.out.println("Missile finalize for " + this);
    }
}
