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

    void sendUpdatePacketToServer(Packet230ModLoader updatePacket)
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
    
    abstract void applyUpdatePacket(Packet230ModLoader updatePacket);
    
    void log(String s)
    {
        entity.log(s);
    }
}
