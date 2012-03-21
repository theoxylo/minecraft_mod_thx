package net.minecraft.src;

public class ThxEntityHelper
{
    ThxEntity entity;
    
    Object model; // for client rendering
    
    // CLIENT methods
    // CLIENT methods
    // CLIENT methods
    
    boolean isPaused()
    {
        throw new RuntimeException("not implemented");
    }

    void addChatMessage(String string)
    {
        throw new RuntimeException("not implemented");
    }

    void applyUpdatePacket(Packet230ModLoader updatePacket)
    {
        // to be overridden by client and server specific subclasses
    }
    
    void sendUpdatePacketToServer(Packet230ModLoader updatePacket)
    {
        throw new RuntimeException("not implemented");
    }

    void spawn(Packet230ModLoader packet)
    {
        throw new RuntimeException("not implemented");
    }

    // SERVER methods
    // SERVER methods
    // SERVER methods
    
    Packet230ModLoader getSpawnPacket()
    {
        throw new RuntimeException("not implemented");
    }
    
    // COMMON methods
    // COMMON methods
    // COMMON methods
    
    void log(String s)
    {
        entity.log(s);
    }
}
