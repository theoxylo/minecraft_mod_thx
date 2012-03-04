package net.minecraft.src;

public class ThxEntityMissile extends ThxEntity implements ISpawnable
{
	static int instanceCount = 0;

    final float MISSILE_ACCEL = .6f;
    final float GRAVITY       = .002f;
    
    final int maxAge = 6000;
    
    final float exhaustDelay = .04f;
    float exhaustTimer = 0f;
    
    boolean launched;
    Vector3 thrust;
    
    public ThxEntityHelicopter targetHelicopter;

    public ThxEntityMissile(World world)
    {
        super(world);
        
	    MAX_VELOCITY  = .90f;
	    
        model = new ThxModelMissile();

        setSize(0.25f, 0.25f);

        instanceCount++;
        log("Created ThxEntityMissile instance: " + instanceCount);
        
	    thrust = new Vector3();

        NET_PACKET_TYPE = 77;
        
        //System.out.println(toString() + " - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }

    public ThxEntityMissile(World world, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        this(world);
        log("launching missile");

        setPositionAndRotation(x, y, z, yaw, pitch);
        
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
    }

    @Override
    public void onUpdate()
    {
        if (ticksExisted > 500) 
        {
            setEntityDead();
            return;
        }
	        
        super.onUpdate();
        
        if (!launched)
        {
            launched = true;
	        worldObj.playSoundAtEntity(this, "mob.ghast.fireball", 1f, 1f);
        }

        exhaustTimer -= deltaTime;
        if (exhaustTimer < 0f)
        {
            exhaustTimer = exhaustDelay;
            worldObj.spawnParticle("largesmoke", posX, posY, posZ, 0.0, 0.0, 0.0);
        }
        
        // guide missile to target helicopter
        Vector3 toTarget = new Vector3();
        if (targetHelicopter != null)
        {
            toTarget.set((float)(targetHelicopter.posX - posX), (float)(targetHelicopter.posY - posY), (float)(targetHelicopter.posZ - posZ));
        }
        toTarget.normalize();
        toTarget = toTarget.scale(.05f);
        
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
        Vector3 motion = new Vector3((float)motionX, (float)motionY, (float)motionZ);
        
        moveEntity(motionX, motionY, motionZ); // this will update posX, posY, posZ
        
        float dx = (float)(posX - prevPosX);
        float dy = (float)(posY - prevPosY);
        float dz = (float)(posZ - prevPosZ);
        Vector3 dPos = new Vector3(dx, dy, dz);
        
        // if movement was blocked by hitting something,
        // then dPos will be less than motion
        Vector3 courseChange = Vector3.subtract(dPos, motion, null);
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
