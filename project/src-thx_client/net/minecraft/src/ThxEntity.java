package net.minecraft.src;

import net.minecraft.client.Minecraft;

public class ThxEntity extends Entity implements ISpawnable
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI = 03.14159265f;

    Minecraft minecraft;

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

    ThxModel model;

    Vector3 pos;
    Vector3 vel;
    Vector3 ypr;

    Vector3 fwd;
    Vector3 side;
    Vector3 up;

    boolean inWater;

    GuiScreen guiScreen;
    public boolean paused;

    public ThxEntity(World world)
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

        minecraft = ModLoader.getMinecraftInstance();
    }

    @Override
    public void onUpdate()
    {
        ticksExisted++;

        // super.onUpdate();

        if (!worldObj.isRemote) // can only pause in single-player mode
        {
            if (guiScreen != minecraft.currentScreen)
            {
                // guiScreen has changed
                guiScreen = minecraft.currentScreen;

                if (guiScreen != null && guiScreen.doesGuiPauseGame())
                {
                    // log("game paused " + this);
                    paused = true;
                }
                else if (paused) // cancel paused
                {
                    // log("game UN-paused " + this);
                    paused = false;
                    prevTime = System.nanoTime();
                }
            }
        }

        long time = System.nanoTime();
        deltaTime = ((float) (time - prevTime)) / 1000000000f; // convert to sec
        prevTime = time;

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;

        prevRotationRoll = rotationRoll;

        updateRotation();
        updateVectors();

        // check for water
        inWater = worldObj.isAABBInMaterial(boundingBox.expand(.0, -.4, .0), Material.water);
    }

    /*
     * Normalize all rotations to -180 to +180 degrees (typically only yaw is
     * affected)
     */
    public void updateRotation()
    {
        rotationYaw %= 360f;
        if (rotationYaw > 180f)
            rotationYaw -= 360f;
        else if (rotationYaw < -180f)
            rotationYaw += 360f;
        yawRad = rotationYaw * RAD_PER_DEG;

        rotationPitch %= 360f;
        if (rotationPitch > 180f)
            rotationPitch -= 360f;
        else if (rotationPitch < -180f)
            rotationPitch += 360f;
        pitchRad = rotationPitch * RAD_PER_DEG;

        rotationRoll %= 360f;
        if (rotationRoll > 180f)
            rotationRoll -= 360f;
        else if (rotationRoll < -180f)
            rotationRoll += 360f;
        rollRad = rotationRoll * RAD_PER_DEG;

        // plog("rotationYaw: " + rotationYaw + ", rotationPitch: " +
        // rotationPitch + ", rotationRoll: " + rotationRoll);
    }

    public void updateVectors()
    {
        float cosRoll = (float) MathHelper.cos(-rollRad);
        float sinRoll = (float) MathHelper.sin(-rollRad);
        float sinYaw = (float) MathHelper.sin(-yawRad - PI);
        float cosYaw = (float) MathHelper.cos(-yawRad - PI);
        float sinPitch = (float) MathHelper.sin(-pitchRad);
        float cosPitch = (float) MathHelper.cos(-pitchRad);

        fwd.x = -sinYaw * cosPitch;
        fwd.y = sinPitch;
        fwd.z = -cosYaw * cosPitch;

        // System.out.println(this + ": forward delta: " + Vector3.sub(fwd,
        // getForward(), null));

        side.x = cosYaw * cosRoll;
        side.y = -sinRoll;
        side.z = -sinYaw * cosRoll;

        // up.x = cosYaw * sinRoll - sinYaw * sinPitch * cosRoll;
        // up.y = cosPitch * cosRoll;
        // up.z = -sinYaw * sinRoll - sinPitch * cosRoll * cosYaw;

        Vector3.cross(fwd, side, up);

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

    public Vector3 getForward()
    {
        float f3 = MathHelper.sin(-rotationYaw * 0.01745329F - 3.141593F);
        float f1 = MathHelper.cos(-rotationYaw * 0.01745329F - 3.141593F);
        float f5 = -MathHelper.cos(-rotationPitch * 0.01745329F);
        float f7 = MathHelper.sin(-rotationPitch * 0.01745329F);
        return new Vector3(f3 * f5, f7, f1 * f5);
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
        // log("setEntityDead called");
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

    /** ISpawnable client interface */
    @Override
    public void spawn(Packet230ModLoader packet)
    {
        log("Received spawn packet: " + packet);

        // refresh ever actually needed?
        minecraft = ModLoader.getMinecraftInstance();

        int entityIdOrig = entityId;

        entityId = packet.dataInt[0];

        applyUpdatePacketFromServer(packet);

        log("spawn with pos, rot, mot, and id for entity with previous id " + entityIdOrig);
        log("spawn(): posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }

    public void applyUpdatePacketFromServer(Packet230ModLoader packet)
    {
        int pilotEntityId = packet.dataInt[1];
        
        if (pilotEntityId == 0)
        {
            log("Skipping server update with no pilot");
            return;
        }
        
        if (riddenByEntity != null && riddenByEntity.entityId == minecraft.thePlayer.entityId)
        {
            if (pilotEntityId != minecraft.thePlayer.entityId) log("ignoring server update that would replace player pilot");
            
            log("Skipping server update for player pilot entity " + riddenByEntity.entityId);
            return;
        }
        
        // no or wrong current pilot
        if (riddenByEntity == null || riddenByEntity.entityId != pilotEntityId)
        {
            Entity pilot = ((WorldClient) worldObj).getEntityByID(pilotEntityId);
            if (pilot != null && !pilot.isDead)
            {
                log("applyUpdatePacket: pilot " + pilot + " now boarding");
                pilot.mountEntity(this);
            }
        }

        serverPosX = (int) packet.dataFloat[0] * 32;
        serverPosY = (int) packet.dataFloat[1] * 32;
        serverPosZ = (int) packet.dataFloat[2] * 32;

        setPosition(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2]);
        setRotation(packet.dataFloat[3], packet.dataFloat[4]);

        rotationRoll = packet.dataFloat[5] % 360f;

        // for now, clear any motion
        motionX = .0; // packet.dataFloat[6];
        motionY = .0; // packet.dataFloat[7];
        motionZ = .0; // packet.dataFloat[8];
    }

    public void sendUpdatePacketToServer()
    {
        if (!worldObj.isRemote)
            return;

        // only the pilot player can send updates to the server
        if (!minecraft.thePlayer.equals(riddenByEntity))
            return;

        Packet230ModLoader packet = new Packet230ModLoader();

        packet.modId = mod_Thx.instance.getId();
        packet.packetType = 75; // entityNetId;

        packet.dataString = new String[] { "thx update packet for entity " + entityId };

        packet.dataInt = new int[2];
        packet.dataInt[0] = entityId;
        packet.dataInt[1] = riddenByEntity.entityId;

        packet.dataFloat = new float[9];
        packet.dataFloat[0] = (float) posX;
        packet.dataFloat[1] = (float) posY;
        packet.dataFloat[2] = (float) posZ;
        packet.dataFloat[3] = rotationYaw;
        packet.dataFloat[4] = rotationPitch;
        packet.dataFloat[5] = rotationRoll;
        packet.dataFloat[6] = (float) motionX;
        packet.dataFloat[7] = (float) motionY;
        packet.dataFloat[8] = (float) motionZ;

        minecraft.getSendQueue().addToSendQueue(packet);

        log("Sent update packet: " + packet.modId + "." + packet.packetType + ", posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }
}
