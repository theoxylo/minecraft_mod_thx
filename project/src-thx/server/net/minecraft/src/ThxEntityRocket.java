package net.minecraft.src;

import java.util.List;
import java.util.Random;

public class ThxEntityRocket extends ThxEntityRocketBase implements ISpawnable
{
    public ThxEntityRocket(World world)
    {
        super(world);
    }

    public ThxEntityRocket(Entity owner, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        super(owner, x, y, z, dx, dy, dz, yaw, pitch);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    @Override
    ThxEntityHelper createHelper()
    {
	    return new ThxEntityHelperServer(this);
    }
}

