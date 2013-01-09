package net.minecraft.src;

public class ThxEntityHelperClient extends ThxEntityHelper
{
    GuiScreen guiScreen;
    
    ThxModel model; // for client rendering
    
    ThxEntityHelperClient(ThxEntity e, ThxModel m)
    {
        entity = e;
        model = m;
    }
    
    @Override
    Object getModel()
    {
        return model;
    }
    
    @Override
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
}
