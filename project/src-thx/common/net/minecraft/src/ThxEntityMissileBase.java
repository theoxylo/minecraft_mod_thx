package net.minecraft.src;

public abstract class ThxEntityMissileBase extends ThxEntity
{
    final float MAX_ACCEL    = 0.60f;
    //final float MAX_VELOCITY = 0.60f;
    final float MAX_VELOCITY = 0.50f;
    final float GRAVITY      = 0.005f;

    final int MAX_AGE_TICKS = 600; // there are 20 ticks in a second
    
    final float EXHAUST_DELAY = .04f;
    
    float exhaustTimer = 0f;
    
    boolean launched;
    Vector3 thrust;
    
    public ThxEntityHelicopter targetHelicopter;

    abstract ThxEntityHelper createHelper();
    
    public ThxEntityMissileBase(World world)
    {
        super(world);
        
        helper = createHelper();
        
        setSize(0.25f, 0.25f);

	    thrust = new Vector3();

        NET_PACKET_TYPE = 77;
    }

    public ThxEntityMissileBase(World world, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        this(world);
        
        setPositionAndRotation(x, y, z, yaw, pitch);
        
        updateRotation();
        updateVectors();
        
        log("fwd: " + fwd + ", side: " + side + ", up: " + up);
                
        // initial thrust + owner "momentum"
        thrust.x = (float)(fwd.x * MAX_ACCEL + dx);
        thrust.y = (float)(fwd.y * MAX_ACCEL + dy);
        thrust.z = (float)(fwd.z * MAX_ACCEL + dz);
        
        motionX = thrust.x;
        motionY = thrust.y;
        motionZ = thrust.z;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        
        if (!launched)
        {
            launched = true;
	        worldObj.playSoundAtEntity(this, "mob.ghast.fireball", 1f, 1f);
        }

        exhaustTimer -= deltaTime;
        if (exhaustTimer < 0f)
        {
            exhaustTimer = EXHAUST_DELAY;
            worldObj.spawnParticle("largesmoke", posX, posY, posZ, 0.0, 0.0, 0.0);
        }
        worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0, 0.0, 0.0);
        
        // guide missile to target helicopter
        Vector3 toTarget = new Vector3();
        if (targetHelicopter != null)
        {
            toTarget.set((float)(targetHelicopter.posX - posX), (float)(targetHelicopter.posY - posY), (float)(targetHelicopter.posZ - posZ));
        }
        toTarget.normalize();
        toTarget = toTarget.scale(.05f);
        
        // gravity pull
        motionY -= GRAVITY * deltaTime;

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
        
        // set yaw and pitch to match movement
        setHeading:
        {
	        //float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
	        //prevRotationYaw = rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
	        //prevRotationPitch = rotationPitch = (float)((Math.atan2(motionY, f) * 180D) / 3.1415927410125732D);
            
	        //float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
	        //rotationYaw = (float)(((Math.atan2(motionX, motionZ) * 180.0) + 90.0) / 3.1415927410125732D);
	        //rotationPitch = (float)((Math.atan2(motionY, f) * 180.0) / 3.1415927410125732D);
        }

        
        float dx = (float)(posX - prevPosX);
        float dy = (float)(posY - prevPosY);
        float dz = (float)(posZ - prevPosZ);
        Vector3 dPos = new Vector3(dx, dy, dz);
        
        // if movement was blocked by hitting something,
        // then dPos will be less than motion
        Vector3 courseChange = Vector3.subtract(dPos, motion, null);
        if (courseChange.lengthSquared() > .001 || ticksExisted > MAX_AGE_TICKS)
        {
            float power = 2f;
            boolean flaming = false;
            worldObj.newExplosion(this, posX, posY, posZ, power, flaming);
            setEntityDead();
        }
    }
}
