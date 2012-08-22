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
        //if (!world.isRemote) // can only pause in single-player mode {
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
        //}
        return false;
    }
    
    @Override
    void addChatMessageToAll(String s)
    {
        minecraft.ingameGUI.getChatGUI().printChatMessage(s);
    }

    void addChatMessageToPilot(String s)
    {
        // only the pilot player should see this message
        if (!minecraft.thePlayer.equals(entity.riddenByEntity)) return;

        minecraft.ingameGUI.getChatGUI().printChatMessage(s);
    }

    void sendUpdatePacketToServer(Packet230ModLoader packet)
    {
        // TODO: detect if mp game? if (!world.isRemote) return;

        // only the pilot player can send updates to the server
        if (!minecraft.thePlayer.equals(entity.riddenByEntity)) return;

        //log("Sending update packet: " + packet);
        minecraft.getSendQueue().addToSendQueue(packet);
    }

    void applyUpdatePacket(Packet230ModLoader packet)
    {
        if (packet == null) return;
        
        int ownerId = packet.dataInt[1];
        if (ownerId > 0)
    	{
            if (entity.owner == null || entity.owner.entityId != ownerId)
            {
                log("*** Entity owner id: " + ownerId);
                
            	// first check for owner that is entity (e.g. helicopter)
            	entity.owner = ((WorldClient) world).getEntityByID(ownerId);
                
                if (entity.owner == null)
                {
                    // otherwise, check for owner that is a player
                	entity.owner = mod_Thx.getEntityPlayerById(ownerId);
                }
                
                log("*** Entity owner: " + entity.owner);
            }
    	}

        int packetPilotId = packet.dataInt[2];
        // no or wrong current pilot
        if (packetPilotId > 0 && (entity.riddenByEntity == null || entity.riddenByEntity.entityId != packetPilotId))
        {
            Entity pilot = ((WorldClient) world).getEntityByID(packetPilotId);
            if (pilot != null && !pilot.isDead)
            {
                log("*** applyUpdatePacket: pilot " + pilot + " now boarding");
                pilot.mountEntity(entity); // boarding
            }
        }
        else if (packetPilotId == 0 && entity.riddenByEntity != null)
        {
            log("*** current pilot id " + entity.riddenByEntity.entityId + " is exiting");
            //entity.riddenByEntity.mountEntity(entity); // unmount
            entity.pilotExit();
        }
        
        entity.serverPosX = MathHelper.floor_float(packet.dataFloat[0] * 32f);
        entity.serverPosY = MathHelper.floor_float(packet.dataFloat[1] * 32f);
        entity.serverPosZ = MathHelper.floor_float(packet.dataFloat[2] * 32f);
    }
}
