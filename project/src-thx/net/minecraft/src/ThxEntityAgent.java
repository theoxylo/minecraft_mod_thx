package net.minecraft.src;

import org.lwjgl.util.vector.Vector3f;

public class ThxEntityAgent extends ThxEntity
{
    final float MISSILE_ACCEL = .14f;
    final float MAX_VELOCITY  = .90f;
    final float GRAVITY       = .002f;
    
    final int maxAge = 6000;
    
    Vector3f thrust;
    
    boolean enableHeavyWeapons = false;
    
    public boolean visible = true;

    public ThxEntityAgent(World world)
    {
        super(world);
        
        setSize(0.25f, 0.25f);

	    thrust = new Vector3f();
	    
        //System.out.println(toString() + " - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }

    public ThxEntityAgent(World world, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
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

        worldObj.playSoundAtEntity(this, "mob.ghast.fireball", 1f, 1f);
    }

    @Override
    protected void entityInit()
    {
        // log("EntityThxAgent entityInit called");
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        
        // short circuit for testing model
        //if (true) return;
        
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        
        // gravity pull
        //if (motionY > -.09) motionY -= GRAVITY;
        motionY -= GRAVITY * dT;

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
        if (courseChange.lengthSquared() > .001 || ticksExisted > maxAge)
        {
            float power = 1.7f;
            if (enableHeavyWeapons) power = 20;
            worldObj.newExplosion(this, posX, posY, posZ, power, true);
            setEntityDead();
        }
    }
}
