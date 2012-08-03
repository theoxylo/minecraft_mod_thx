package net.minecraft.server;

public interface ISpawnable
{
    Packet250CustomPayload getSpawnPacket();
}
