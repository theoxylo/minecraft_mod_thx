package net.minecraft.server;

public class ThxEntityMissile extends ThxEntityMissileBase implements ISpawnable
{
    public ThxEntityMissile(World var1)
    {
        super(var1);
    }

    public ThxEntityMissile(Entity var1, double var2, double var4, double var6, double var8, double var10, double var12, float var14, float var15)
    {
        super(var1, var2, var4, var6, var8, var10, var12, var14, var15);
    }

    ThxEntityHelper createHelper()
    {
        return new ThxEntityHelperServer(this);
    }
}
