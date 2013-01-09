package net.minecraft.src;

public abstract class ThxEntityHelper
{
    ThxEntity entity;
    
    Object getModel()
    {
        return null;
    }
    
    boolean isPaused()
    {
        return false;
    }

    void addChatMessageToAll(String string)
    {
    }

    void addChatMessageToPilot(String string)
    {
    }
    
    void updateAnimation()
    {
    }
    
    void pilotExit(Entity pilot)
    {
    }
    
    void onUpdateWithPilot()
    {
    }
}
