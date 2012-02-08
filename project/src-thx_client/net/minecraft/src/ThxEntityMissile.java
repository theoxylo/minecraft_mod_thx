package net.minecraft.src;

import org.lwjgl.util.vector.Vector3f;

public class ThxEntityMissile extends ThxEntity
{
    static int instanceCount = 0;

    final float MISSILE_ACCEL = .6f;
    final float MAX_VELOCITY  = .90f;
    final float GRAVITY       = .002f;
    
    final int maxAge = 6000;
    final float exhaustTimer = .04f;
    float exhaustTime = 0f;
    
    Vector3f thrust;
    
    public ThxEntityHelicopter targetHelicopter;

    public ThxEntityMissile(World world)
    {
        super(world);
        
        model = new ThxModelMissile();

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
        thrust.x = (float)(fwd.x * MISSILE_ACCEL + dx);
        thrust.y = (float)(fwd.y * MISSILE_ACCEL + dy);
        thrust.z = (float)(fwd.z * MISSILE_ACCEL + dz);
        
        motionX = thrust.x;
        motionY = thrust.y;
        motionZ = thrust.z;

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
        super.onUpdate();
        
        exhaustTime += deltaTime;
        if (exhaustTime > exhaustTimer)
        {
            exhaustTime = 0f;
            worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0, 0.0, 0.0);
        }
        
        // guide missile to target helicopter
        Vector3f toTarget = new Vector3f();
        if (targetHelicopter != null) toTarget.set((float)(targetHelicopter.posX - posX), (float)(targetHelicopter.posY - posY), (float)(targetHelicopter.posZ - posZ));
        toTarget = (Vector3f)toTarget.scale(1f);
        toTarget = (Vector3f)toTarget.scale(.05f);
        

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        
        // gravity pull
        //if (motionY > -.09) motionY -= GRAVITY;
        motionY -= GRAVITY * deltaTime / .05f;

        // following is a cheap check but note that it doesn't
        // allow for any way to slow or change course once max'ed
        if (vel.lengthSquared() < MAX_VELOCITY * MAX_VELOCITY)
        {
            motionX += thrust.x * deltaTime + toTarget.x;
            motionY += thrust.y * deltaTime + toTarget.y;
            motionZ += thrust.z * deltaTime + toTarget.z;
        }
        Vector3f motion = new Vector3f((float)motionX, (float)motionY, (float)motionZ);
        
        moveEntity(motionX, motionY, motionZ);
        
        float dx = (float)(posX - prevPosX);
        float dy = (float)(posY - prevPosY);
        float dz = (float)(posZ - prevPosZ);
        Vector3f dPos = new Vector3f(dx, dy, dz);
        
        // if movement was blocked by hitting something,
        // then dPos will be less than motion
        Vector3f courseChange = Vector3f.sub(dPos, motion, null);
        if (courseChange.lengthSquared() > .001 || ticksExisted > maxAge)
        {
            float power = 2f;
            boolean flaming = false;
            worldObj.newExplosion(this, posX, posY, posZ, power, flaming);
            setEntityDead();
            
            // light torch?
            //Item.itemsList[50].onItemUse(new ItemStack(), this, posX, posY, posZ
        }

        // gradual constant pitch down until 20 deg
        //if (rotationPitch < 20f) rotationPitch += .4f;
        
        // following is not working, off by 90 deg?
        //float f1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        //prevRotationYaw = rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
        //prevRotationPitch = rotationPitch = (float)((Math.atan2(motionY, f1) * 180D) / 3.1415927410125732D);
    }
}
