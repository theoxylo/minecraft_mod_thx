package net.minecraft.src;

public abstract class ThxEntityHelper
{
    ThxEntity entity;
    
    Object model; // for client rendering
    
    // CLIENT methods
    // CLIENT methods
    // CLIENT methods
    
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

    // SERVER methods
    // SERVER methods
    // SERVER methods
    
    /*
    Packet230ModLoader getSpawnPacket()
    {
        throw new RuntimeException("not implemented");
    }
    */
    
    // COMMON methods
    // COMMON methods
    // COMMON methods
    
    void log(String s)
    {
        entity.log(s);
    }
}
