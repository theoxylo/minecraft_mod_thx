package net.minecraft.src;

import java.util.List;
import java.util.Random;

public class ThxEntityRocket extends ThxEntityRocketBase implements ISpawnable
{
    public ThxEntityRocket(World world)
    {
        super(world);
        System.out.println("C1 ThxEntityRocket called");
    }

    public ThxEntityRocket(Entity owner, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        super(owner, x, y, z, dx, dy, dz, yaw, pitch);
        System.out.println("C2 ThxEntityRocket called");
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    /* from ISpawnable interface */
    public Packet230ModLoader getSpawnPacket()
    {
        return helper.getSpawnPacket();
    }
        
    @Override
    ThxEntityHelper createHelper()
    {
	    return new ThxEntityHelperServer(this);
    }
}

