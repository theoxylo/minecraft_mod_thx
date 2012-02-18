package net.minecraft.src;

public abstract class ThxEntityBase extends Entity
{
    boolean plog = true; // enable periodic logging for rapidly repeating events

    final float RAD_PER_DEG = 00.01745329f;
    final float PI = 03.14159265f;

    long prevTime;
    float deltaTime;

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

    Vector3 pos;
    Vector3 vel;
    Vector3 ypr;

    Vector3 fwd;
    Vector3 side;
    Vector3 up;
    
    int NET_PACKET_TYPE;
    float damage;
    float throttle;
    int fire1;
    int fire2;

    public ThxEntityBase(World world)
    {
        super(world);

        log(world.isRemote ? "Created new MP entity" : "Created new SP entity");

        preventEntitySpawning = true;

        // vectors relative to entity orientation
        fwd = new Vector3();
        side = new Vector3();
        up = new Vector3();

        pos = new Vector3();
        vel = new Vector3();
        ypr = new Vector3();

        prevTime = System.nanoTime();
    }

    @Override
    public void onUpdate()
    {
        ticksExisted++;
        plog(String.format("start onUpdate, pilot %d [posX: %5.2f, posY: %5.2f, posZ: %5.2f, yaw: %5.2f]", getPilotId(), posX, posY, posZ, rotationYaw));
        
        long time = System.nanoTime();
        deltaTime = ((float) (time - prevTime)) / 1000000000f; // convert to sec
        prevTime = time;

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;
        prevRotationRoll = rotationRoll;
    }
    
    public int getPilotId()
    {
        return riddenByEntity != null ? riddenByEntity.entityId : 0;
    }
    
    public boolean isInWater()
    {
        // check for contact with water
        return worldObj.isAABBInMaterial(boundingBox.expand(.0, -.4, .0), Material.water);
    }
    
    /*
     * Normalize all rotations to -180 to +180 degrees (typically only yaw is * affected)
     */
    public void updateRotation()
    {
        rotationYaw %= 360f;
        if (rotationYaw > 180f) rotationYaw -= 360f;
        else if (rotationYaw < -180f) rotationYaw += 360f;
        yawRad = rotationYaw * RAD_PER_DEG;

        rotationPitch %= 360f;
        if (rotationPitch > 180f) rotationPitch -= 360f;
        else if (rotationPitch < -180f) rotationPitch += 360f;
        pitchRad = rotationPitch * RAD_PER_DEG;

        rotationRoll %= 360f;
        if (rotationRoll > 180f) rotationRoll -= 360f;
        else if (rotationRoll < -180f) rotationRoll += 360f;
        rollRad = rotationRoll * RAD_PER_DEG;
    }

    public void updateVectors()
    {
        float cosYaw   = (float) MathHelper.cos(-yawRad - PI);
        float sinYaw   = (float) MathHelper.sin(-yawRad - PI);
        float cosPitch = (float) MathHelper.cos(-pitchRad);
        float sinPitch = (float) MathHelper.sin(-pitchRad);
        float cosRoll  = (float) MathHelper.cos(-rollRad);
        float sinRoll  = (float) MathHelper.sin(-rollRad);

        fwd.x = -sinYaw * cosPitch;
        fwd.y = sinPitch;
        fwd.z = -cosYaw * cosPitch;

        side.x = cosYaw * cosRoll;
        side.y = -sinRoll;
        side.z = -sinYaw * cosRoll;

        // up.x = cosYaw * sinRoll - sinYaw * sinPitch * cosRoll;
        // up.y = cosPitch * cosRoll;
        // up.z = -sinYaw * sinRoll - sinPitch * cosRoll * cosYaw;
        Vector3.cross(fwd, side, up);

        pos.x = (float) posX;
        pos.y = (float) posY;
        pos.z = (float) posZ;

        vel.x = (float) motionX;
        vel.y = (float) motionY;
        vel.z = (float) motionZ;

        ypr.x = rotationYaw;
        ypr.y = rotationPitch;
        ypr.z = rotationRoll;
    }

    public Vector3 getForward()
    {
        float f3 = MathHelper.sin(-rotationYaw * 0.01745329F - 3.141593F);
        float f1 = MathHelper.cos(-rotationYaw * 0.01745329F - 3.141593F);
        float f5 = -MathHelper.cos(-rotationPitch * 0.01745329F);
        float f7 = MathHelper.sin(-rotationPitch * 0.01745329F);
        return new Vector3(f3 * f5, f7, f1 * f5);
    }

    @Override
    public void setEntityDead()
    {
        log("setEntityDead called");
        super.setEntityDead();
    }

    @Override
    protected void fall(float f)
    {
        // no damage from falling, unlike super.fall
        log("fall() called with arg " + f);
    }
    
    /* abstract methods from Entity base class */
    @Override
    protected void entityInit()
    {
        log("entityInit called");
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        //log("readEntityFromNBT called");
    }
    
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        //log("writeEntityToNBT called");
    }
    
    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " " + entityId;
    }

    void log(String s)
    {
        System.out.println(String.format("[%4d] ", ticksExisted) + this + ": " + s);
    }
    
    void plog(String s) // periodic log
    {
        if (plog && ticksExisted % 60 == 0)
        {
            log(s);
        }
    }
    
    public Packet230ModLoader getUpdatePacket()
    {
        Packet230ModLoader packet = new Packet230ModLoader();

        packet.modId = mod_Thx.instance.getId();
        packet.packetType = NET_PACKET_TYPE;

        packet.dataString = new String[] { "thx update packet for tick " + ticksExisted };

        packet.dataInt = new int[4];
        packet.dataInt[0] = entityId;
        packet.dataInt[1] = riddenByEntity != null ? riddenByEntity.entityId : 0;
        packet.dataInt[2] = fire1;
        packet.dataInt[3] = fire2;
        
        // clear fire flags after use
        fire1 = 0;
        fire2 = 0;

        packet.dataFloat = new float[11];
        packet.dataFloat[0] = (float) posX;
        packet.dataFloat[1] = (float) posY;
        packet.dataFloat[2] = (float) posZ;
        packet.dataFloat[3] = rotationYaw;
        packet.dataFloat[4] = rotationPitch;
        packet.dataFloat[5] = rotationRoll;
        packet.dataFloat[6] = (float) motionX;
        packet.dataFloat[7] = (float) motionY;
        packet.dataFloat[8] = (float) motionZ;
        packet.dataFloat[9] = damage;
        packet.dataFloat[10] = throttle;
        
        return packet;
    }
}
