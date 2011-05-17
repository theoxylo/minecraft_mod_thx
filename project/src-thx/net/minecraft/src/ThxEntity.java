package net.minecraft.src;

import org.lwjgl.input.Keyboard;

public class ThxEntity extends Entity
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;

    boolean plog = true;

    public boolean visible = true;
    
    double prevMotionX;
    double prevMotionY;
    double prevMotionZ;

    public float prevRotationRoll;
    public float rotationRoll;
    
    public String renderTexture;
    
    public ModelBase renderModel;
    
    static int instanceCount;

    public ThxEntity(World world)
    {
        super(world);
        instanceCount++;
        log("ThxEntity() called " + instanceCount);
        
        preventEntitySpawning = true;

        prevPosX = posX = 0.0;
        prevPosY = posY = 0.0;
        prevPosZ = posZ = 0.0;

        prevMotionX = motionX = 0.0;
        prevMotionY = motionY = 0.0;
        prevMotionZ = motionZ = 0.0;
        
        prevRotationYaw   = rotationYaw   = 0f;
        prevRotationPitch = rotationPitch = 0f;
        prevRotationRoll  = rotationRoll  = 0f;
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        
        plog("onUpdate called, ticksExisted:" + ticksExisted);
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
    
    public String toString()
    {
        return this.getClass().getSimpleName() + " " + entityId;
    }
}
