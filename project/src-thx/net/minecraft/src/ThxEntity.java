package net.minecraft.src;

import org.lwjgl.util.vector.Vector3f;

public class ThxEntity extends Entity
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;

    boolean plog = true;

    boolean visible = true;
    
    double prevMotionX;
    double prevMotionY;
    double prevMotionZ;

    float rotationRoll;
    float prevRotationRoll;
    
    boolean rotationDidChange;
    
    float yawRad;
    float pitchRad;
    float rollRad;

    String renderTexture;
    
    ThxModel model;
    
    Vector3f fwd;
    Vector3f side;
    Vector3f up;
    
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
        
        yawRad   = prevRotationYaw   = rotationYaw   = 0f;
        pitchRad = prevRotationPitch = rotationPitch = 0f;
        rollRad  = prevRotationRoll  = rotationRoll  = 0f;
        
	    fwd = new Vector3f(1f, 0f, 0f);
	    side = new Vector3f(0f, 0f, 1f);
	    up = new Vector3f(0f, -1f, 0f);
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();

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

        /*
        rotationDidChange = false;
        
        float deltaYaw = rotationYaw - prevRotationYaw;
        if (deltaYaw > 180f) deltaYaw -= 360f;
        else if (deltaYaw < -180f) deltaYaw += 360f;
        if (Math.abs(deltaYaw) > .001f)
        {
            prevRotationYaw = rotationYaw;
	        yawRad = rotationYaw   * RAD_PER_DEG;
            rotationDidChange = true;
        }
        float deltaPitch = rotationPitch - prevRotationPitch;
        if (deltaPitch > 180f) deltaPitch -= 360f;
        else if (deltaPitch < -180f) deltaPitch += 360f;
        if (Math.abs(deltaPitch) > .001f || rotationDidChange)
        {
            prevRotationPitch = rotationPitch;
	        pitchRad = rotationPitch * RAD_PER_DEG;
            rotationDidChange = true;
        }
        float deltaRoll = rotationRoll - prevRotationRoll;
        if (deltaRoll > 180f) deltaRoll -= 360f;
        else if (deltaRoll < -180f) deltaRoll += 360f;
        if (Math.abs(deltaRoll) > .001f || rotationDidChange)
        {
            prevRotationRoll = rotationRoll;
	        rollRad = rotationRoll  * RAD_PER_DEG;
            rotationDidChange = true;
        }
        
        if (rotationDidChange)
        {
        }
        */
        
        float cosYaw = MathHelper.cos(yawRad);
        float cosPitch = MathHelper.cos(-pitchRad);
        float cosRoll = MathHelper.cos(rollRad);
        float sinYaw = MathHelper.sin(yawRad);
        float sinPitch = MathHelper.sin(-pitchRad);
        float sinRoll = MathHelper.sin(rollRad);
            
        fwd.x = sinYaw * cosPitch;
        fwd.y = sinPitch;
        fwd.z = -cosYaw * cosPitch;
            
        up.x = -cosYaw * sinRoll - sinYaw * sinPitch * cosRoll; 
        up.y = cosPitch * cosRoll;
        up.z = -sinYaw * sinRoll - sinPitch * cosRoll * -cosYaw;
            
        Vector3f.cross(fwd, up, side);
    }
    
    @Override
    public float getShadowSize()
    {
        return 0.0F;
    }

    @Override
    public void setEntityDead()
    {
        log("setEntityDead called");
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
        EntityChicken entitychicken = new EntityChicken(worldObj);
        entitychicken.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
        worldObj.entityJoinedWorld(entitychicken);
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

    void vectorAdd(Vec3D vec, Vec3D arg)
    {
        vec.xCoord += arg.xCoord;
        vec.yCoord += arg.yCoord;
        vec.zCoord += arg.zCoord;
    }

    void vectorScale(Vec3D vec, double factor)
    {
        double lengthSq = vec.xCoord * vec.xCoord + vec.yCoord * vec.yCoord + vec.zCoord * vec.zCoord;

        // if (lengthSq < .0001 && factor < 1.0) return; // already very short

        vec.xCoord *= factor;
        vec.yCoord *= factor;
        vec.zCoord *= factor;
    }

    void vectorLimit(Vec3D vec, double max)
    {
        double lengthSq = vec.xCoord * vec.xCoord + vec.yCoord * vec.yCoord + vec.zCoord * vec.zCoord;

        // if (lengthSq < .0001) return; // already very short

        if (lengthSq > max * max)
        {
            double scale = max / Math.sqrt(lengthSq);
            vec.xCoord *= scale;
            vec.yCoord *= scale;
            vec.zCoord *= scale;
        }
    }

    void vectorSetLength(Vec3D vec, double newLength)
    {
        double lengthSq = vec.xCoord * vec.xCoord + vec.yCoord * vec.yCoord + vec.zCoord * vec.zCoord;

        // if (lengthSq < .0001) return; // already very short

        if (lengthSq > newLength * newLength)
        {
            double scale = newLength / Math.sqrt(lengthSq);
            vec.xCoord *= scale;
            vec.yCoord *= scale;
            vec.zCoord *= scale;
        }
    }
}
