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
    
    /* from ISpawnable interface */
    @Override
    public void spawn(Packet230ModLoader packet)
    {
        helper.spawn(packet);
    }
    
    @Override
    ThxEntityHelper createHelper()
    {
        ThxModel model = new ThxModelMissile();
        overrideMissileModel:
        {
	        model.renderTexture = "/thx/rocket.png";
	        model.rotationRollSpeed = 90f; // units?
        }
        return new ThxEntityHelperClient(this, model);
    }
}

