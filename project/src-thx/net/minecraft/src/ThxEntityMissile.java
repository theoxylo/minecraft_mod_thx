package net.minecraft.src;

import org.lwjgl.util.vector.Vector3f;

public class ThxEntityMissile extends ThxEntity
{
    static int instanceCount = 0;

    final float MISSILE_ACCEL = .10f;
    final float MAX_VELOCITY  = .70f;
    final float GRAVITY       = .01f;
    
    Vector3f thrust;
    
    boolean launched = false;

    public ThxEntityMissile(World world)
    {
        super(world);
        
        model = new ThxModelMissile();
        renderTexture = "/thx/missile.png";

        setSize(0.25f, 0.25f);

        instanceCount++;
        log("Created ThxEntityMissile instance: " + instanceCount);
        
	    thrust = new Vector3f();
	    
        //System.out.println(toString() + " - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }

    public ThxEntityMissile(World world, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        this(world);
        log("launching missile");

        setPosition(x, y, z);
        rotationYaw = yaw;
        rotationPitch = pitch;
        
        updateRotation();
        updateVectors();
        
        log("fwd: " + fwd + ", side: " + side + ", up: " + up);
                
        // initial thrust + owner "momentum"
        thrust.x = (float) (fwd.x * MISSILE_ACCEL * 2f + dx);
        thrust.y = (float) (fwd.y * MISSILE_ACCEL * 2f + dy);
        thrust.z = (float) (fwd.z * MISSILE_ACCEL * 2f + dz);

        // give an extra height boost
        thrust.y += .01f;
        
        // set initial position out a bit
        // set previous pos to detect when stopped
        //double start = 5.0;
        //prevPosX = posX = x + motionX * start;
        //prevPosY = posY = y + motionY * start;
        //prevPosZ = posZ = z + motionZ * start;
        
        //log("posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
        //log("motionX: " + motionX + ", motionY: " + motionY + ", motionZ: " + motionZ);
        
        log("constructor done");

        worldObj.playSoundAtEntity(this, "mob.ghast.fireball", 1f, 1f);
    }

    @Override
    protected void entityInit()
    {
        // log("EntityThxMissile entityInit called");
    }

    @Override
    public void onUpdate()
    {
        //if (!launched) return;
        
        super.onUpdate();
        
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        
        // gravity pull
        if (motionY > -.09) motionY -= GRAVITY;

        // following is a cheap check but note that it doesn't
        // allow for any way to slow or change course once max'ed
        if (vel.lengthSquared() < MAX_VELOCITY * MAX_VELOCITY)
        {
	        motionX += thrust.x;
	        motionY += thrust.y;
	        motionZ += thrust.z;
        }
        Vector3f motion = new Vector3f((float)motionX, (float)motionY, (float)motionZ);
        
        moveEntity(motionX, motionY, motionZ);
        
        float dx = (float)(posX - prevPosX);
        float dy = (float)(posY - prevPosY);
        float dz = (float)(posZ - prevPosZ);
        Vector3f dPos = new Vector3f(dx, dy, dz);
        
        Vector3f courseChange = Vector3f.sub(dPos, motion, null);
        if (courseChange.lengthSquared() > .001)
        {
            worldObj.newExplosion(this, posX, posY, posZ, 1.0F, true);
            setEntityDead();
            //System.out.println("missile has stopped, deltaPosSQ: " + deltaPosSqXZ + ", deltaY: " + dy);
        }

        // gradual constant pitch down until 20 deg
        if (rotationPitch < 20f) rotationPitch += .4f;
        
        // spiral
        rotationRoll += 9f;
    }

    @Override
    public String toString()
    {
        return "Missile " + entityId;
    }

    /*
    @Override
    public void finalize()
    {
        System.out.println("Missile finalize for " + this);
    }
    */
}
