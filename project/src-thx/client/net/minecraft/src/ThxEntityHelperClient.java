package net.minecraft.src;

import net.minecraft.client.Minecraft;

public class ThxEntityHelperClient extends ThxEntityHelper
{
    World world;
    Minecraft minecraft;
    GuiScreen guiScreen;
    
    ThxEntityHelperClient(ThxEntity entity, ThxModel model)
    {
        this.model = model;
        this.entity = entity;
        world = entity.worldObj;
        minecraft = ModLoader.getMinecraftInstance();
    }
    
    boolean isPaused()
    {
        if (!world.isRemote) // can only pause in single-player mode
        {
            if (guiScreen != minecraft.currentScreen)
            {
                // guiScreen has changed
                guiScreen = minecraft.currentScreen;

                if (guiScreen != null && guiScreen.doesGuiPauseGame())
                {
                    // log("game paused " + this);
                    return true;
                }
            }
        }
        return false;
    }
    
    void addChatMessage(String s)
    {
        minecraft.ingameGUI.addChatMessage(s);
    }

    void sendUpdatePacketToServer(Packet230ModLoader packet)
    {
        if (!world.isRemote) return;

        // only the pilot player can send updates to the server
        if (!minecraft.thePlayer.equals(entity.riddenByEntity)) return;

        //log("Sending update packet: " + packet);
        minecraft.getSendQueue().addToSendQueue(packet);
    }
    
    void spawn(Packet230ModLoader packet)
    {
        log("Received spawn packet: " + packet);

        int entityIdOrig = entity.entityId;

        entity.entityId = packet.dataInt[0];

        entity.latestUpdatePacket = packet;
        applyUpdatePacketFromServer();
        
        entity.updateRotation();
        entity.updateVectors();

        log("spawn with pos, rot, mot, and id for entity with previous id " + entityIdOrig);
        log("spawn(): posX: " + entity.posX + ", posY: " + entity.posY + ", posZ: " + entity.posZ);
    }

    void applyUpdatePacketFromServer()
    {
        if (entity.latestUpdatePacket == null) return;
        
        Packet230ModLoader packet = entity.latestUpdatePacket;
        entity.latestUpdatePacket = null;
        
        entity.plog("applyUpdatePacketFromServer: " + packet);
        
        int packetPilotId = packet.dataInt[1];
        
        // no or wrong current pilot
        if (packetPilotId > 0 && (entity.riddenByEntity == null || entity.riddenByEntity.entityId != packetPilotId))
        {
            Entity pilot = ((WorldClient) world).getEntityByID(packetPilotId);
            if (pilot != null && !pilot.isDead)
            {
                log("applyUpdatePacket: pilot " + pilot + " now boarding");
                pilot.mountEntity(entity);
            }
        }
        else if (packetPilotId == 0 && entity.riddenByEntity != null)
        {
            log("current pilot id " + entity.riddenByEntity.entityId + " is exiting");
            entity.riddenByEntity.mountEntity(entity);
        }
        
        
        // ignore fire controls, only used by server to spawn projectiles
        //fire1 = packet.dataInt[2];
        //fire2 = packet.dataInt[3];
        
        int ownerId = packet.dataInt[4];
        if (ownerId > 0) entity.owner = ((WorldClient) world).getEntityByID(ownerId);
        log("Entity owner: " + entity.owner);

        entity.serverPosX = MathHelper.floor_float(packet.dataFloat[0] * 32f);
        entity.serverPosY = MathHelper.floor_float(packet.dataFloat[1] * 32f);
        entity.serverPosZ = MathHelper.floor_float(packet.dataFloat[2] * 32f);
        
        entity.setPositionAndRotation(packet.dataFloat[0], packet.dataFloat[1], packet.dataFloat[2], packet.dataFloat[3], packet.dataFloat[4]);
        
        entity.rotationRoll = packet.dataFloat[5] % 360f;

        // for now, clear any motion
        entity.motionX = packet.dataFloat[6];
        entity.motionY = packet.dataFloat[7];
        entity.motionZ = packet.dataFloat[8];
        
        entity.damage = packet.dataFloat[9];
        entity.throttle = packet.dataFloat[10];
    }
}
