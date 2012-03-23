package net.minecraft.src;

public class ThxEntityMissile extends ThxEntityMissileBase implements ISpawnable
{
    public ThxEntityMissile(World world)
    {
        super(world);
    }

    public ThxEntityMissile(World world, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        super(world, x, y, z, dx, dy, dz, yaw, pitch);
    }
    
    @Override
    ThxEntityHelper createHelper()
    {
	    return new ThxEntityHelperServer(this);
    }
}
