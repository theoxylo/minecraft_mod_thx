package net.minecraft.src;

public class ThxEntityMissile extends ThxEntity
{
    
    final int MISSILE_RECHARGE = 90;
    final double MISSILE_ACCEL = .40;
    final double MAX_VELOCITY  = .50;
    final double GRAVITY       = .005;
    
    static int instanceCount = 0;

    // total update count
    int missileDelay = 0;
    boolean stopped = true;
    
    //Vec3D deltaPos;
    
    static ThxEntityMissile _instance = null;

    public static ThxEntityMissile getInstance(World world)
    {
        if (_instance == null)
        {
            _instance = new ThxEntityMissile(world);
            world.entityJoinedWorld(_instance);
        }
        return _instance;
    }
    
    public ThxEntityMissile(World world)
    {
        super(world);

        renderModel = new ThxModelMissile();
        renderTexture = "/thx/missile.png";
        
        setSize(.5f, .5f);
        
	    missileDelay = 0;
	    stopped = true;
	    
        //deltaPos = Vec3D.createVector(.0, .0, .0);

        if (_instance == null) _instance = this;
        
        new Exception("new ThxEntityMissile called").printStackTrace();
        
        instanceCount++;
        log("EntityThxMissile instance count: " + instanceCount);
    }

    @Override
    protected void entityInit()
    {
        //log("EntityThxMissile entityInit called");
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        
        // start by moving entity
        //double speedSq = motionX*motionX + motionY*motionY + motionZ*motionZ;
        //if (speedSq > .05) moveEntity(motionX, motionY, motionZ);
        moveEntity(motionX, motionY, motionZ);
      
        if (missileDelay > 0) missileDelay--;

        
        //deltaPos.xCoord = posX - prevPosX;
        //deltaPos.yCoord = posY - prevPosY;
        //deltaPos.zCoord = posZ - prevPosZ;
        
        double dx = posX - prevPosX;
        double dy = posY - prevPosY;
        double dz = posZ - prevPosZ;
        //double deltaPosSq = dx*dx + dy*dy + dz*dz;
        double deltaPosSqXZ = dx*dx + dz*dz;
        
        if (!stopped)
        {
            // detonate if we hit an obstacle
            if (deltaPosSqXZ < .10 && dy*dy < .05)
            {
                worldObj.newExplosion(this, posX, posY, posZ, 1.0F, true);

                stopped = true;

                visible = false;
                
                prevMotionX = motionX = .0;
                prevMotionY = motionY = .0;
                prevMotionZ = motionZ = .0;

                //System.out.println("missile has stopped, deltaPosSQ: " + deltaPosSqXZ + ", deltaY: " + dy);
            }
            
            // pitch down
            if (rotationPitch < 20f) rotationPitch += .4f;
            
            // spiral
            rotationRoll += 9f;

            // gravity pull
            if (motionY > -.09) motionY -= GRAVITY;
        }
       
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

    void launch(double x, double y, double z, 
                double dx, double dy, double dz, float yaw, float pitch)
    {
        log("missile launch called");
        
        if (missileDelay == 0)
        {
            missileDelay = MISSILE_RECHARGE;
            stopped = false;
            visible = true;
            
            float yawRad = yaw * RAD_PER_DEG;
            float pitchRad = pitch * RAD_PER_DEG;
            
            float f1 = MathHelper.cos(-yawRad - PI);
            float f3 = MathHelper.sin(-yawRad - PI);
            float f5 = -MathHelper.cos(-pitchRad);
            float f7 = MathHelper.sin(-pitchRad);
            /*
            Vec3D thrust = Vec3D.createVector(f3 * f5, f7, f1 * f5);
            log("Missile thrust: " + thrust + ", speed: " + thrust.lengthVector());
            */
            
            prevMotionX = motionX = f3 * f5 * MISSILE_ACCEL + dx;
            prevMotionY = motionY = f7      * MISSILE_ACCEL + dy;
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

            worldObj.playSoundAtEntity(this, "mob.ghast.fireball", 1.0f, 1.0f);
            log("missile was launched");
        }
        else log("missile delay remaining: " + missileDelay);
    }
    
    public String toString() { return "Missile " + entityId; }
}
