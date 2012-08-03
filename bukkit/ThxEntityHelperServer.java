package net.minecraft.server;

public class ThxEntityHelperServer extends ThxEntityHelper
{
    public ThxEntityHelperServer(ThxEntity var1)
    {
        this.entity = var1;
    }

    void applyUpdatePacket(int var1, int var2, float var3, float var4, float var5) {}

    void addChatMessageToAll(String var1)
    {
        this.log("Chat broadcast: " + var1);
    }
}
