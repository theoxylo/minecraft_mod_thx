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

}
