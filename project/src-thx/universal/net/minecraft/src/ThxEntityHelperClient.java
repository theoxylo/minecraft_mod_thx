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
}
