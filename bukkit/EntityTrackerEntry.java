package net.minecraft.server;

import forge.ForgeHooks;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EntityTrackerEntry
{
    /** The entity that this EntityTrackerEntry tracks. */
    public Entity tracker;
    public int b;
    public int field_9234_e;

    /** The encoded entity X position. */
    public int xLoc;

    /** The encoded entity Y position. */
    public int yLoc;

    /** The encoded entity Z position. */
    public int zLoc;

    /** The encoded entity yaw rotation. */
    public int yRot;

    /** The encoded entity pitch rotation. */
    public int xRot;
    public int field_48617_i;
    public double j;
    public double k;
    public double l;
    public int m = 0;
    private double p;
    private double q;
    private double r;
    private boolean s = false;
    private boolean isMoving;
    private int field_28165_t = 0;
    public boolean n = false;
    public Set trackedPlayers = new HashSet();

    public EntityTrackerEntry(Entity par1Entity, int par2, int par3, boolean par4)
    {
        this.tracker = par1Entity;
        this.b = par2;
        this.field_9234_e = par3;
        this.isMoving = par4;
        this.xLoc = MathHelper.floor(par1Entity.locX * 32.0D);
        this.yLoc = MathHelper.floor(par1Entity.locY * 32.0D);
        this.zLoc = MathHelper.floor(par1Entity.locZ * 32.0D);
        this.yRot = MathHelper.d(par1Entity.yaw * 256.0F / 360.0F);
        this.xRot = MathHelper.d(par1Entity.pitch * 256.0F / 360.0F);
        this.field_48617_i = MathHelper.d(par1Entity.ar() * 256.0F / 360.0F);
    }

    public boolean equals(Object par1Obj)
    {
        return par1Obj instanceof EntityTrackerEntry ? ((EntityTrackerEntry)par1Obj).tracker.id == this.tracker.id : false;
    }

    public int hashCode()
    {
        return this.tracker.id;
    }

    public void track(List par1List)
    {
        this.n = false;

        if (!this.s || this.tracker.e(this.p, this.q, this.r) > 16.0D)
        {
            this.p = this.tracker.locX;
            this.q = this.tracker.locY;
            this.r = this.tracker.locZ;
            this.s = true;
            this.n = true;
            this.scanPlayers(par1List);
        }

        ++this.field_28165_t;

        if (this.m++ % this.field_9234_e == 0 || this.tracker.ce)
        {
            if (this.tracker instanceof IClientDriven && ((ThxEntity)this.tracker).isActive)
            {
                Packet250CustomPayload var23 = ((IClientDriven)this.tracker).getUpdatePacket();
                Iterator var24 = this.trackedPlayers.iterator();

                while (var24.hasNext())
                {
                    Object var25 = var24.next();

                    if (!var25.equals(this.tracker.passenger))
                    {
                        ((EntityPlayer)var25).netServerHandler.sendPacket(var23);
                    }
                }

                this.xLoc = MathHelper.floor(this.tracker.locX * 32.0D);
                this.yLoc = MathHelper.floor(this.tracker.locY * 32.0D);
                this.zLoc = MathHelper.floor(this.tracker.locZ * 32.0D);
                this.yRot = MathHelper.d(this.tracker.yaw * 256.0F / 360.0F);
                this.xRot = MathHelper.d(this.tracker.pitch * 256.0F / 360.0F);
                return;
            }

            int var2 = MathHelper.floor(this.tracker.locX * 32.0D);
            int var3 = MathHelper.floor(this.tracker.locY * 32.0D);
            int var4 = MathHelper.floor(this.tracker.locZ * 32.0D);
            int var5 = MathHelper.d(this.tracker.yaw * 256.0F / 360.0F);
            int var6 = MathHelper.d(this.tracker.pitch * 256.0F / 360.0F);
            int var7 = var2 - this.xLoc;
            int var8 = var3 - this.yLoc;
            int var9 = var4 - this.zLoc;
            Object var10 = null;
            boolean var11 = Math.abs(var7) >= 4 || Math.abs(var8) >= 4 || Math.abs(var9) >= 4;
            boolean var12 = Math.abs(var5 - this.yRot) >= 4 || Math.abs(var6 - this.xRot) >= 4;

            if (var7 >= -128 && var7 < 128 && var8 >= -128 && var8 < 128 && var9 >= -128 && var9 < 128 && this.field_28165_t <= 400)
            {
                if (var11 && var12)
                {
                    var10 = new Packet33RelEntityMoveLook(this.tracker.id, (byte)var7, (byte)var8, (byte)var9, (byte)var5, (byte)var6);
                }
                else if (var11)
                {
                    var10 = new Packet31RelEntityMove(this.tracker.id, (byte)var7, (byte)var8, (byte)var9);
                }
                else if (var12)
                {
                    var10 = new Packet32EntityLook(this.tracker.id, (byte)var5, (byte)var6);
                }
            }
            else
            {
                this.field_28165_t = 0;
                this.tracker.locX = (double)var2 / 32.0D;
                this.tracker.locY = (double)var3 / 32.0D;
                this.tracker.locZ = (double)var4 / 32.0D;
                var10 = new Packet34EntityTeleport(this.tracker.id, var2, var3, var4, (byte)var5, (byte)var6);
            }

            if (this.isMoving)
            {
                double var13 = this.tracker.motX - this.j;
                double var15 = this.tracker.motY - this.k;
                double var17 = this.tracker.motZ - this.l;
                double var19 = 0.02D;
                double var21 = var13 * var13 + var15 * var15 + var17 * var17;

                if (var21 > var19 * var19 || var21 > 0.0D && this.tracker.motX == 0.0D && this.tracker.motY == 0.0D && this.tracker.motZ == 0.0D)
                {
                    this.j = this.tracker.motX;
                    this.k = this.tracker.motY;
                    this.l = this.tracker.motZ;
                    this.broadcast(new Packet28EntityVelocity(this.tracker.id, this.j, this.k, this.l));
                }
            }

            if (var10 != null)
            {
                this.broadcast((Packet)var10);
            }

            DataWatcher var26 = this.tracker.getDataWatcher();

            if (var26.a())
            {
                this.broadcastIncludingSelf(new Packet40EntityMetadata(this.tracker.id, var26));
            }

            int var14 = MathHelper.d(this.tracker.ar() * 256.0F / 360.0F);

            if (Math.abs(var14 - this.field_48617_i) >= 4)
            {
                this.broadcast(new Packet35EntityHeadRotation(this.tracker.id, (byte)var14));
                this.field_48617_i = var14;
            }

            if (var11)
            {
                this.xLoc = var2;
                this.yLoc = var3;
                this.zLoc = var4;
            }

            if (var12)
            {
                this.yRot = var5;
                this.xRot = var6;
            }
        }

        this.tracker.ce = false;

        if (this.tracker.velocityChanged)
        {
            this.broadcastIncludingSelf(new Packet28EntityVelocity(this.tracker));
            this.tracker.velocityChanged = false;
        }
    }

    public void broadcast(Packet par1Packet)
    {
        Iterator var3 = this.trackedPlayers.iterator();

        while (var3.hasNext())
        {
            EntityPlayer var2 = (EntityPlayer)var3.next();
            var2.netServerHandler.sendPacket(par1Packet);
        }
    }

    public void broadcastIncludingSelf(Packet par1Packet)
    {
        this.broadcast(par1Packet);

        if (this.tracker instanceof EntityPlayer)
        {
            ((EntityPlayer)this.tracker).netServerHandler.sendPacket(par1Packet);
        }
    }

    public void a()
    {
        this.broadcast(new Packet29DestroyEntity(this.tracker.id));
    }

    public void a(EntityPlayer var1)
    {
        if (this.trackedPlayers.contains(var1))
        {
            this.trackedPlayers.remove(var1);
        }
    }

    public void updatePlayer(EntityPlayer var1)
    {
        if (var1 != this.tracker)
        {
            double var2 = var1.locX - (double)(this.xLoc / 32);
            double var4 = var1.locZ - (double)(this.zLoc / 32);

            if (var2 >= (double)(-this.b) && var2 <= (double)this.b && var4 >= (double)(-this.b) && var4 <= (double)this.b)
            {
                if (!this.trackedPlayers.contains(var1))
                {
                    this.trackedPlayers.add(var1);
                    var1.netServerHandler.sendPacket(this.b());

                    if (this.tracker instanceof IClientDriven)
                    {
                        return;
                    }

                    if (this.isMoving)
                    {
                        var1.netServerHandler.sendPacket(new Packet28EntityVelocity(this.tracker.id, this.tracker.motX, this.tracker.motY, this.tracker.motZ));
                    }

                    ItemStack[] var6 = this.tracker.getEquipment();

                    if (var6 != null)
                    {
                        for (int var7 = 0; var7 < var6.length; ++var7)
                        {
                            var1.netServerHandler.sendPacket(new Packet5EntityEquipment(this.tracker.id, var7, var6[var7]));
                        }
                    }

                    if (this.tracker instanceof EntityHuman)
                    {
                        EntityHuman var11 = (EntityHuman)this.tracker;

                        if (var11.isSleeping())
                        {
                            var1.netServerHandler.sendPacket(new Packet17EntityLocationAction(this.tracker, 0, MathHelper.floor(this.tracker.locX), MathHelper.floor(this.tracker.locY), MathHelper.floor(this.tracker.locZ)));
                        }
                    }

                    if (this.tracker instanceof EntityLiving)
                    {
                        EntityLiving var10 = (EntityLiving)this.tracker;
                        Iterator var9 = var10.getEffects().iterator();

                        while (var9.hasNext())
                        {
                            MobEffect var8 = (MobEffect)var9.next();
                            var1.netServerHandler.sendPacket(new Packet41MobEffect(this.tracker.id, var8));
                        }
                    }
                }
            }
            else if (this.trackedPlayers.contains(var1))
            {
                this.trackedPlayers.remove(var1);
                var1.netServerHandler.sendPacket(new Packet29DestroyEntity(this.tracker.id));
            }
        }
    }

    public void scanPlayers(List par1List)
    {
        for (int var2 = 0; var2 < par1List.size(); ++var2)
        {
            this.updatePlayer((EntityPlayer)par1List.get(var2));
        }
    }

    private Packet b()
    {
        if (this.tracker.dead)
        {
            System.out.println("Fetching addPacket for removed entity");
        }

        Packet var1 = ForgeHooks.getEntitySpawnPacket(this.tracker);

        if (var1 != null)
        {
            return var1;
        }
        else if (this.tracker instanceof EntityItem)
        {
            EntityItem var8 = (EntityItem)this.tracker;
            Packet21PickupSpawn var9 = new Packet21PickupSpawn(var8);
            var8.locX = (double)var9.b / 32.0D;
            var8.locY = (double)var9.c / 32.0D;
            var8.locZ = (double)var9.d / 32.0D;
            return var9;
        }
        else if (this.tracker instanceof EntityPlayer)
        {
            return new Packet20NamedEntitySpawn((EntityHuman)this.tracker);
        }
        else
        {
            if (this.tracker instanceof EntityMinecart)
            {
                EntityMinecart var2 = (EntityMinecart)this.tracker;

                if (var2.type == 0)
                {
                    return new Packet23VehicleSpawn(this.tracker, 10);
                }

                if (var2.type == 1)
                {
                    return new Packet23VehicleSpawn(this.tracker, 11);
                }

                if (var2.type == 2)
                {
                    return new Packet23VehicleSpawn(this.tracker, 12);
                }
            }

            if (this.tracker instanceof EntityBoat)
            {
                return new Packet23VehicleSpawn(this.tracker, 1);
            }
            else if (this.tracker instanceof IAnimal)
            {
                return new Packet24MobSpawn((EntityLiving)this.tracker);
            }
            else if (this.tracker instanceof EntityEnderDragon)
            {
                return new Packet24MobSpawn((EntityLiving)this.tracker);
            }
            else if (this.tracker instanceof EntityFishingHook)
            {
                return new Packet23VehicleSpawn(this.tracker, 90);
            }
            else if (this.tracker instanceof EntityArrow)
            {
                Entity var7 = ((EntityArrow)this.tracker).shooter;
                return new Packet23VehicleSpawn(this.tracker, 60, var7 != null ? var7.id : this.tracker.id);
            }
            else if (this.tracker instanceof EntitySnowball)
            {
                return new Packet23VehicleSpawn(this.tracker, 61);
            }
            else if (this.tracker instanceof EntityPotion)
            {
                return new Packet23VehicleSpawn(this.tracker, 73, ((EntityPotion)this.tracker).getPotionValue());
            }
            else if (this.tracker instanceof EntityThrownExpBottle)
            {
                return new Packet23VehicleSpawn(this.tracker, 75);
            }
            else if (this.tracker instanceof EntityEnderPearl)
            {
                return new Packet23VehicleSpawn(this.tracker, 65);
            }
            else if (this.tracker instanceof EntityEnderSignal)
            {
                return new Packet23VehicleSpawn(this.tracker, 72);
            }
            else
            {
                Packet23VehicleSpawn var3;

                if (this.tracker instanceof EntitySmallFireball)
                {
                    EntitySmallFireball var6 = (EntitySmallFireball)this.tracker;
                    var3 = null;

                    if (var6.shooter != null)
                    {
                        var3 = new Packet23VehicleSpawn(this.tracker, 64, var6.shooter.id);
                    }
                    else
                    {
                        var3 = new Packet23VehicleSpawn(this.tracker, 64, 0);
                    }

                    var3.e = (int)(var6.dirX * 8000.0D);
                    var3.f = (int)(var6.dirY * 8000.0D);
                    var3.g = (int)(var6.dirZ * 8000.0D);
                    return var3;
                }
                else if (this.tracker instanceof EntityFireball)
                {
                    EntityFireball var5 = (EntityFireball)this.tracker;
                    var3 = null;

                    if (var5.shooter != null)
                    {
                        var3 = new Packet23VehicleSpawn(this.tracker, 63, ((EntityFireball)this.tracker).shooter.id);
                    }
                    else
                    {
                        var3 = new Packet23VehicleSpawn(this.tracker, 63, 0);
                    }

                    var3.e = (int)(var5.dirX * 8000.0D);
                    var3.f = (int)(var5.dirY * 8000.0D);
                    var3.g = (int)(var5.dirZ * 8000.0D);
                    return var3;
                }
                else if (this.tracker instanceof EntityEgg)
                {
                    return new Packet23VehicleSpawn(this.tracker, 62);
                }
                else if (this.tracker instanceof EntityTNTPrimed)
                {
                    return new Packet23VehicleSpawn(this.tracker, 50);
                }
                else if (this.tracker instanceof EntityEnderCrystal)
                {
                    return new Packet23VehicleSpawn(this.tracker, 51);
                }
                else
                {
                    if (this.tracker instanceof EntityFallingBlock)
                    {
                        EntityFallingBlock var4 = (EntityFallingBlock)this.tracker;

                        if (var4.id == Block.SAND.id)
                        {
                            return new Packet23VehicleSpawn(this.tracker, 70);
                        }

                        if (var4.id == Block.GRAVEL.id)
                        {
                            return new Packet23VehicleSpawn(this.tracker, 71);
                        }

                        if (var4.id == Block.DRAGON_EGG.id)
                        {
                            return new Packet23VehicleSpawn(this.tracker, 74);
                        }
                    }

                    if (this.tracker instanceof EntityPainting)
                    {
                        return new Packet25EntityPainting((EntityPainting)this.tracker);
                    }
                    else if (this.tracker instanceof EntityExperienceOrb)
                    {
                        return new Packet26AddExpOrb((EntityExperienceOrb)this.tracker);
                    }
                    else
                    {
                        throw new IllegalArgumentException("Don\'t know how to add " + this.tracker.getClass() + "!");
                    }
                }
            }
        }
    }

    /**
     * Remove a tracked player from our list and tell the tracked player to destroy us from their world.
     */
    public void clear(EntityPlayer var1)
    {
        if (this.trackedPlayers.contains(var1))
        {
            this.trackedPlayers.remove(var1);
            var1.netServerHandler.sendPacket(new Packet29DestroyEntity(this.tracker.id));
        }
    }
}
