package net.minecraft.src;


public class ThxEntity extends Entity //implements ISpawnable
{
    public Entity owner;
    
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;

    boolean plog = true;

    boolean visible = true;
    
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

    String renderTexture;
    
    Vector3 pos;
    Vector3 vel;
    //Vector3 ypr;
    
    Vector3 fwd;
    Vector3 side;
    Vector3 up;
    
    boolean inWater;
    
   
    public ThxEntity(World world)
    {
        super(world);
        
        preventEntitySpawning = true;

        // vectors relative to entity orientation
        fwd = new Vector3();
        side = new Vector3();
        up = new Vector3();

        pos = new Vector3();
        vel = new Vector3();
        // ypr = new Vector3();

        prevTime = System.nanoTime();
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        long time = System.nanoTime();
        deltaTime = ((float)(time - prevTime)) / 1000000000f; // convert to sec
        prevTime = time;
        
        updateRotation();
        updateVectors();
        
        // check for water
        inWater = worldObj.isAABBInMaterial(boundingBox.expand(.0, -.4, .0), Material.water);
    }
 
    /*
     *  Normalize all rotations to -180 to +180 degrees (typically only yaw is affected)
     */
    public void updateRotation()
    {
        rotationYaw   %= 360f;
        if (rotationYaw > 180f) rotationYaw -= 360f;
        else if (rotationYaw < -180f) rotationYaw += 360f;
        yawRad = rotationYaw * RAD_PER_DEG;
        
        rotationPitch %= 360f;
        if (rotationPitch > 180f) rotationPitch -= 360f;
        else if (rotationPitch < -180f) rotationPitch += 360f;
        pitchRad = rotationPitch * RAD_PER_DEG;
        
        rotationRoll  %= 360f;
        if (rotationRoll > 180f) rotationRoll -= 360f;
        else if (rotationRoll < -180f) rotationRoll += 360f;
        rollRad = rotationRoll * RAD_PER_DEG;
        
        //plog("rotationYaw: " + rotationYaw + ", rotationPitch: " + rotationPitch + ", rotationRoll: " + rotationRoll);
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
        
        //System.out.println(this + ": forward delta: " + Vector3.sub(fwd, getForward(), null));
        
        side.x = cosYaw * cosRoll;
        side.y = -sinRoll;
        side.z = -sinYaw * cosRoll;
        
        //up.x = cosYaw * sinRoll - sinYaw * sinPitch * cosRoll; 
        //up.y = cosPitch * cosRoll;
        //up.z = -sinYaw * sinRoll - sinPitch * cosRoll * cosYaw;
            
        Vector3.cross(fwd, side, up);
        
        // refresh 
        pos.x = (float) posX;
        pos.y = (float) posY;
        pos.z = (float) posZ;
        
        vel.x = (float) motionX;
        vel.y = (float) motionY;
        vel.z = (float) motionZ;
        
        //ypr.z = rotationYaw;
        //ypr.y = rotationPitch;
        //ypr.z = rotationRoll;
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

    void plog(String s) // periodic log
    {
        if (plog && ticksExisted % 60 == 0)
        {
            log(s);
        }
    }
    
    void log(String s)
    {        
        ThxConfig.log(this + ": " + s);
    }
    
    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " " + entityId;
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
        log("readEntityFromNBT called");
    }
    
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        log("writeEntityToNBT called");
    }
    
    /* from ISpawnable interface 
     * 
     * Passed to the client to update local copy with new entity id, pos, etc
     * */
    //@Override
    /*
    public Packet230ModLoader getSpawnPacket()
    {
        Packet230ModLoader packet = new Packet230ModLoader();
        
        packet.dataInt = new int[1];
        packet.dataInt[0] = entityId;
        
        packet.dataFloat = new float[9];
        packet.dataFloat[0] = (float) posX;
        packet.dataFloat[1] = (float) posY;
        packet.dataFloat[2] = (float) posZ;
        packet.dataFloat[3] = rotationYaw;
        packet.dataFloat[4] = rotationPitch;
        packet.dataFloat[5] = rotationYaw;
        packet.dataFloat[6] = (float) motionX;
        packet.dataFloat[7] = (float) motionY;
        packet.dataFloat[8] = (float) motionZ;
        
        log("Created new spawn packet: " + packet);
        
        return packet;
    }
    */
    
    public void handleUpdatePacket(Packet230ModLoader packet)
    {
        log("handleUpdatePacket: " + packet.modId + "." + packet.packetType);
        
        if (packet.dataInt == null || packet.dataInt.length == 0) 
        {
            log("Ignore update packet without entity id");
            return;
        }
        if (entityId != packet.dataInt[0])
        {
            log("Ignoring update packet for entity " + packet.dataInt[0]);
            return;
        }
        int entityIdOrig = entityId;
        
        entityId = packet.dataInt[0];
        
        lastTickPosX = prevPosX = posX = packet.dataFloat[0];
        lastTickPosY = prevPosY = posY = packet.dataFloat[1];
        lastTickPosY = prevPosZ = posZ = packet.dataFloat[2];
        setPositionAndRotation(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2], packet.dataFloat[3], packet.dataFloat[4]);
        
        //setLocationAndAngles(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2], packet.dataFloat[3], packet.dataFloat[4]);
        //setPosition(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2]);

        rotationYaw = packet.dataFloat[3];
        rotationPitch = packet.dataFloat[4];
        rotationYaw = packet.dataFloat[5];
        
        motionX = packet.dataFloat[6];
        motionY = packet.dataFloat[7];
        motionZ = packet.dataFloat[8];
        
        log("Updated pos, rot, mot, and id for entity with previous id " + entityIdOrig + ", posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }
    
    public void sendPacket(EntityPlayerMP player, int type, String msg)
    {
        log("Sending packet type " + type);
        
        Packet230ModLoader packet = new Packet230ModLoader();
        packet.modId = mod_Thx.instance.getId();
        packet.packetType = type;
        packet.dataString = new String[]{ msg };
        ModLoaderMp.SendPacketTo(mod_Thx.instance, player, packet);
    }
}
