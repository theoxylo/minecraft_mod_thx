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

    void sendUpdatePacketToServer(Packet250CustomPayload updatePacket)
    {
    }

    // SERVER methods
    // SERVER methods
    // SERVER methods
    
    /*
    Packet250CustomPayload getSpawnPacket()
    {
        throw new RuntimeException("not implemented");
    }
    */
    
    // COMMON methods
    // COMMON methods
    // COMMON methods
    
    abstract void applyUpdatePacket(int ownerId, int packetPilotId, float x, float y, float z);
    
    void log(String s)
    {
        entity.log(s);
    }
}
