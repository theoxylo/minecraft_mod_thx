package net.minecraft.src;

import net.minecraft.client.Minecraft;

public abstract class ThxEntity extends ThxEntityBase implements ISpawnable
{
    Minecraft minecraft;
    Packet230ModLoader latestUpdatePacket;
    boolean visible = true;
    String renderTexture;
    ThxModel model;
    GuiScreen guiScreen;
    boolean paused;

    public ThxEntity(World world)
    {
        super(world);
        minecraft = ModLoader.getMinecraftInstance();
    }

    @Override
    public void onUpdate()
    {
		super.onUpdate();
		
		applyUpdatePacketFromServer();

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
        
        updateRotation();
        updateVectors();
    }

    /** ISpawnable client interface */
    @Override
    public void spawn(Packet230ModLoader packet)
    {
        log("Received spawn packet: " + packet);

        int entityIdOrig = entityId;

        entityId = packet.dataInt[0];

        handleUpdatePacketFromServer(packet);
        applyUpdatePacketFromServer();
        updateRotation();
        updateVectors();

        log("spawn with pos, rot, mot, and id for entity with previous id " + entityIdOrig);
        log("spawn(): posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }

    public void handleUpdatePacketFromServer(Packet230ModLoader packet)
    {
        plog("handleUpdatePacketFromServer: " + packet); // inbound packet not aligned with plog unless very high update rate
        
        int packetPilotId = packet.dataInt[1];
        int player = minecraft.thePlayer.entityId;
        
        if (packetPilotId == 0 && riddenByEntity != null)
        {
            log("current pilot id is missing from update packet. shouldn't happen");
            //log("Skipping server update with no pilot");
            //return;
        }
        
        if (riddenByEntity != null && riddenByEntity.entityId == minecraft.thePlayer.entityId)
        {
            if (packetPilotId != minecraft.thePlayer.entityId) log("ignoring server update that would replace player pilot");
            
            //log("Skipping server update for player pilot entity " + riddenByEntity.entityId);
            return;
        }
        
        latestUpdatePacket = packet;
    }
        
        
    private void applyUpdatePacketFromServer()
    {
        if (latestUpdatePacket == null) return;
        
        Packet230ModLoader packet = latestUpdatePacket;
        latestUpdatePacket = null;
        
        plog("applyUpdatePacketFromServer: " + packet);
        
        int packetPilotId = packet.dataInt[1];
        int player = minecraft.thePlayer.entityId;
        
        // no or wrong current pilot
        if (riddenByEntity == null || riddenByEntity.entityId != packetPilotId)
        {
            Entity pilot = ((WorldClient) worldObj).getEntityByID(packetPilotId);
            if (pilot != null && !pilot.isDead)
            {
                log("applyUpdatePacket: pilot " + pilot + " now boarding");
                pilot.mountEntity(this);
            }
        }

        serverPosX = MathHelper.floor_float(packet.dataFloat[0] * 32f);
        serverPosY = MathHelper.floor_float(packet.dataFloat[1] * 32f);
        serverPosZ = MathHelper.floor_float(packet.dataFloat[2] * 32f);
        
        setPositionAndRotation(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2], packet.dataFloat[3], packet.dataFloat[4]);
        //setPositionAndRotation2(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2], packet.dataFloat[3], packet.dataFloat[4], 0);
        
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

        packet.dataFloat = new float[6];
        packet.dataFloat[0] = (float) posX;
        packet.dataFloat[1] = (float) posY;
        packet.dataFloat[2] = (float) posZ;
        packet.dataFloat[3] = rotationYaw;
        packet.dataFloat[4] = rotationPitch;
        packet.dataFloat[5] = rotationRoll;
        //packet.dataFloat[6] = (float) motionX;
        //packet.dataFloat[7] = (float) motionY;
        //packet.dataFloat[8] = (float) motionZ;

        //log("Sending update packet: " + packet);
        minecraft.getSendQueue().addToSendQueue(packet);
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

    // DEBUGGING OVERRIDES
    // DEBUGGING OVERRIDES
    // DEBUGGING OVERRIDES
    
    /* called by NetClientHandler for Packet28EntityVelocity */
    @Override
    public void setVelocity(double x, double y, double z)
    {
        log("setVelocity (Packet28?)");
        super.setVelocity(x, y, z);
        getDataWatcher();
    }
    
    /* called by NetClientHandler for Packet40EntityMetadata */
    @Override
    public DataWatcher getDataWatcher()
    {
        log("getDataWatcher (Packet40?)");
        return super.getDataWatcher();
    }
    
    /* called by NetClientHandler for Packet30/31/32/33Entity and Packet34EntityTeleport */
    @Override
    public void setPositionAndRotation2(double d, double d1, double d2, float f, float f1, int i)
    {
        log("setPositionAndRotation2 (Packet30/31/32/33Entity and Packet34EntityTeleport?)");
        super.setPositionAndRotation2(d, d1, d2, f, f1, i);
    }
    
    /* called by NetClientHandler for Packet38EntityStatus */
    @Override
    public void handleHealthUpdate(byte b)
    {
        log("handleHealthUpdate (Packet38EntityStatus?)");
        super.handleHealthUpdate(b);
    }
}
