package net.minecraft.server;

public abstract class ThxEntityHelper
{
    ThxEntity entity;
    Object model;

    boolean isPaused()
    {
        return false;
    }

    abstract void addChatMessageToAll(String var1);

    void addChatMessageToPilot(String var1) {}

    void sendUpdatePacketToServer(Packet250CustomPayload var1) {}

    abstract void applyUpdatePacket(int var1, int var2, float var3, float var4, float var5);

    void log(String var1)
    {
        this.entity.log(var1);
    }
}
