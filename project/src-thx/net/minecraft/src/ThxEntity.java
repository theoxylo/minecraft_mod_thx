package net.minecraft.src;

import org.lwjgl.util.vector.Vector3f;

public class ThxEntity extends Entity
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;

    boolean plog = true;

    boolean visible = true;
    
    long prevTime;
    float deltaTime;
    float dT;
    
    double prevMotionX;
    double prevMotionY;
    double prevMotionZ;

    float rotationRoll;
    float prevRotationRoll;
    
    float yawRad;
    float pitchRad;
    float rollRad;
    
    float rotationYawSpeed;
    float rotationPitchSpeed;
    float rotationRollSpeed;

    String renderTexture;
    
    ThxModel model;
    
    Vector3f pos;
    Vector3f vel;
    Vector3f ypr;
    
    Vector3f fwd;
    Vector3f side;
    Vector3f up;
    
    boolean inWater;
    
    public ThxEntity(World world)
    {
        super(world);
        
        preventEntitySpawning = true;

        // lastTickPosX = 0.0; // ???
        // lastTickPosY = 0.0; // ???
        // lastTickPosZ = 0.0; // ???
        
        prevPosX = posX = 0.0;
        prevPosY = posY = 0.0;
        prevPosZ = posZ = 0.0;

        prevMotionX = motionX = 0.0;
        prevMotionY = motionY = 0.0;
        prevMotionZ = motionZ = 0.0;
        
        // new fields available for use by subclasses:
        yawRad   = prevRotationYaw   = rotationYaw   = 0f;
        pitchRad = prevRotationPitch = rotationPitch = 0f;
        rollRad  = prevRotationRoll  = rotationRoll  = 0f;
        
        // vectors relative to entity orientation
	    fwd  = new Vector3f(0f, 0f, 0f);
	    side = new Vector3f(0f, 0f, 0f);
	    up   = new Vector3f(0f, 0f, 0f);
	    
	    pos = new Vector3f(0f, 0f, 0f);
	    vel = new Vector3f(0f, 0f, 0f);
	    ypr = new Vector3f(0f, 0f, 0f);
	    
	    prevTime = System.nanoTime();
    }
    
    @Override
    public void onUpdate()
    {
        long time = System.nanoTime();
        deltaTime = ((float)(time - prevTime)) / 1000000000f; // convert to sec
        dT = deltaTime / .05f; // relative to 20 fps
        prevTime = time;
        //System.out.println("dT: " + dT);
        
        super.onUpdate();
        updateRotation();
        updateVectors();
        
        // check for water
        inWater = worldObj.isAABBInMaterial(boundingBox.expand(.0, -.4, .0), Material.water);
    }
 
    public void updateRotation()
    {
        rotationYaw   %= 360f;
        if (rotationYaw > 180f) rotationYaw -= 360f;
        else if (rotationYaw < -180f) rotationYaw += 360f;
        yawRad = rotationYaw   * RAD_PER_DEG;
        
        rotationPitch %= 360f;
        if (rotationPitch > 180f) rotationPitch -= 360f;
        else if (rotationPitch < -180f) rotationPitch += 360f;
        pitchRad = rotationPitch * RAD_PER_DEG;
        
        rotationRoll  %= 360f;
        if (rotationRoll > 180f) rotationRoll -= 360f;
        else if (rotationRoll < -180f) rotationRoll += 360f;
        rollRad = rotationRoll  * RAD_PER_DEG;
    }
    
    public void updateVectors()
    {
        float cosRoll  = (float) MathHelper.cos(-rollRad);
        float sinRoll  = (float) MathHelper.sin(-rollRad);
        float sinYaw   = (float) MathHelper.sin(-yawRad - PI);
        float cosYaw   = (float) MathHelper.cos(-yawRad - PI);
        float sinPitch = (float) MathHelper.sin(-pitchRad);
        float cosPitch = (float) MathHelper.cos(-pitchRad);
        
        fwd.x = -sinYaw * cosPitch;
        fwd.y = sinPitch;
        fwd.z = -cosYaw * cosPitch;
        
        //System.out.println(this + ": forward delta: " + Vector3f.sub(fwd, getForward(), null));
        
        side.x = cosYaw * cosRoll;
        side.y = -sinRoll;
        side.z = -sinYaw * cosRoll;
        
        //up.x = cosYaw * sinRoll - sinYaw * sinPitch * cosRoll; 
        //up.y = cosPitch * cosRoll;
        //up.z = -sinYaw * sinRoll - sinPitch * cosRoll * cosYaw;
            
        Vector3f.cross(fwd, side, up);
        
        // refresh 
        pos.x = (float) posX;
        pos.y = (float) posY;
        pos.z = (float) posZ;
        
        vel.x = (float) motionX;
        vel.y = (float) motionY;
        vel.z = (float) motionZ;
        
        ypr.z = rotationYaw;
        ypr.y = rotationPitch;
        ypr.z = rotationRoll;
    }
    
    public Vector3f getForward()
    {
        float f3 = MathHelper.sin(-rotationYaw * 0.01745329F - 3.141593F);
        float f1 = MathHelper.cos(-rotationYaw * 0.01745329F - 3.141593F);
        float f5 = -MathHelper.cos(-rotationPitch * 0.01745329F);
        float f7 = MathHelper.sin(-rotationPitch * 0.01745329F);
        return new Vector3f(f3 * f5, f7, f1 * f5);
    }
    
    @Override
    public float getShadowSize()
    {
        return 0.0F;
    }
    
    @Override
    public boolean isInRangeToRenderDist(double d)
    {
        return d < 128.0 * 128.0;
    }
    
    @Override
    public void setEntityDead()
    {
        //log("setEntityDead called");
        super.setEntityDead();
    }

    @Override
    protected void entityInit()
    {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
    }

    void createChicken()
    {
        EntityChicken chicken = new EntityChicken(worldObj);
        chicken.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
        worldObj.entityJoinedWorld(chicken);
    }
    
    void plog(String s) // periodic log
    {
        if (plog && ticksExisted % 60 == 0)
        {
            log(s);
        }
    }
    
    void log(String s)
    {        
        ThxConfig.log(toString() + ": " + s);
    }
    
    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " " + entityId;
    }

}
