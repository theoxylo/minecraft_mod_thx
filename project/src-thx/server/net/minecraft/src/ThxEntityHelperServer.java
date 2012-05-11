package net.minecraft.src;

public class ThxEntityHelperServer extends ThxEntityHelper
{
    public ThxEntityHelperServer(ThxEntity entity)
    {
        this.entity = entity;
    }
    
    // needed?
    void applyUpdatePacket(int ownerId, int packetPilotId, float x, float y, float z)
    {
        return;
    }

    @Override
    void addChatMessageToAll(String s)
    {
        log("Chat broadcast: " + s);
    }
}
