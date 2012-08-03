package net.minecraft.server;

public interface IClientDriven
{
    Packet250CustomPayload getUpdatePacket();
}
