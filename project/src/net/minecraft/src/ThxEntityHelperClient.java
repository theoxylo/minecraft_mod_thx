package net.minecraft.src;


public class ThxEntityHelperClient extends ThxEntityHelper
{
    World world;
    //Minecraft minecraft;
    GuiScreen guiScreen;
    
    ThxEntityHelperClient(ThxEntity entity, ThxModel model)
    {
        this.model = model;
        this.entity = entity;
        world = entity.worldObj;
        //minecraft = FMLClientHandler.instance().getClient();
    }
    
    boolean isPaused()
    {
        if (guiScreen != entity.minecraft.currentScreen)
        {
            // guiScreen has changed
            guiScreen = entity.minecraft.currentScreen;

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
        entity.minecraft.ingameGUI.getChatGUI().printChatMessage(s);
    }

    void addChatMessageToPilot(String s)
    {
        // only the pilot player should see this message
        if (!entity.minecraft.thePlayer.equals(entity.riddenByEntity)) return;

        entity.minecraft.ingameGUI.getChatGUI().printChatMessage(s);
    }
}
