package net.minecraft.server;

import java.util.Iterator;
import java.util.List;

public class ThxEntityHelicopter extends ThxEntityHelicopterBase implements ISpawnable
{
    public ThxEntityHelicopter(World var1)
    {
        super(var1);
    }

    public ThxEntityHelicopter(World var1, double var2, double var4, double var6, float var8)
    {
        super(var1, var2, var4, var6, var8);
    }

    ThxEntityHelper createHelper()
    {
        return new ThxEntityHelperServer(this);
    }

    public void onUpdatePilot()
    {
        int var1 = this.passenger != null ? this.passenger.id : 0;
        this.plog(String.format("onUpdatePilot, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", new Object[] {Integer.valueOf(var1), Double.valueOf(this.locX), Double.valueOf(this.locY), Double.valueOf(this.locZ), Float.valueOf(this.yaw), Float.valueOf(this.throttle), Double.valueOf(this.motX), Double.valueOf(this.motY), Double.valueOf(this.motZ)}));

        if (this.cmd_exit > 0 || this.passenger.dead)
        {
            this.cmd_exit = 0;
            this.pilotExit();
        }

        if (this.cmd_reload > 0)
        {
            this.cmd_reload = 0;
            this.reload();
        }

        if (this.cmd_create_item > 0)
        {
            this.cmd_create_item = 0;
        }

        if (this.cmd_create_map > 0)
        {
            this.cmd_create_map = 0;
            this.createMap();
        }
    }

    void updateMotion(boolean var1)
    {
        if (this.passenger != null)
        {
            this.move(this.motX, this.motY, this.motZ);
        }
        else
        {
            super.updateMotion(var1);
        }
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean damageEntity(DamageSource var1, int var2)
    {
        if (!super.damageEntity(var1, var2))
        {
            return false;
        }
        else
        {
            this.takeDamage((float)var2 * 3.0F);
            return true;
        }
    }

    void pilotExit()
    {
        if (this.passenger != null)
        {
            Packet39AttachEntity var1 = new Packet39AttachEntity(this.passenger, (Entity)null);
            List var2 = ModLoader.getMinecraftServerInstance().serverConfigurationManager.players;
            Iterator var3 = var2.iterator();

            while (var3.hasNext())
            {
                Object var4 = var3.next();
                ((EntityPlayer)var4).netServerHandler.sendPacket(var1);
            }

            double var5 = 1.9D;
            ((EntityPlayer)this.passenger).netServerHandler.a(this.locX + (double)this.fwd.z * var5, this.locY + this.passenger.W(), this.locZ - (double)this.fwd.x * var5, this.yaw, 0.0F);
            super.pilotExit();
        }
    }
}
