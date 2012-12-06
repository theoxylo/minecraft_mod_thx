package net.minecraft.src;

public class ThxEntityMissile extends ThxEntityMissileBase
{
    public ThxEntityMissile(World world)
    {
        super(world);
    }

    public ThxEntityMissile(Entity owner, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        super(owner, x, y, z, dx, dy, dz, yaw, pitch);
    }

    @Override
    ThxEntityHelper createHelper()
    {
        return new ThxEntityHelperClient(this, new ThxModelMissile());
    }
}
