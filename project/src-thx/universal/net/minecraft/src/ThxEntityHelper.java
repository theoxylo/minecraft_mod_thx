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

    abstract void addChatMessageToAll(String string);

    void addChatMessageToPilot(String string)
    {
    }

    void sendUpdatePacketToServer(ThxEntityPacket250 updatePacket)
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
    
    abstract void applyUpdatePacket(ThxEntityPacket250 updatePacket);
    
    void log(String s)
    {
        entity.log(s);
    }
}
