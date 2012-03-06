package net.minecraft.src;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

public class EntityTrackerEntry
{
    /** The entity that this EntityTrackerEntry tracks. */
    public Entity trackedEntity;
    public int trackingDistanceThreshold;
    public int field_9234_e;

    /** The encoded entity X position. */
    public int encodedPosX;

    /** The encoded entity Y position. */
    public int encodedPosY;

    /** The encoded entity Z position. */
    public int encodedPosZ;

    /** The encoded entity yaw rotation. */
    public int encodedRotationYaw;

    /** The encoded entity pitch rotation. */
    public int encodedRotationPitch;
    public int field_48617_i;
    public double lastTrackedEntityMotionX;
    public double lastTrackedEntityMotionY;
    public double lastTrackedEntityMotionZ;
    public int updateCounter;
    private double lastTrackedEntityPosX;
    private double lastTrackedEntityPosY;
    private double lastTrackedEntityPosZ;
    private boolean firstUpdateDone;
    private boolean shouldSendMotionUpdates;
    private int field_28165_t;
    public boolean playerEntitiesUpdated;
    public Set trackedPlayers;

    public EntityTrackerEntry(Entity par1Entity, int par2, int par3, boolean par4)
    {
        updateCounter = 0;
        firstUpdateDone = false;
        field_28165_t = 0;
        playerEntitiesUpdated = false;
        trackedPlayers = new HashSet();
        trackedEntity = par1Entity;
        trackingDistanceThreshold = par2;
        field_9234_e = par3;
        shouldSendMotionUpdates = par4;
        encodedPosX = MathHelper.floor_double(par1Entity.posX * 32D);
        encodedPosY = MathHelper.floor_double(par1Entity.posY * 32D);
        encodedPosZ = MathHelper.floor_double(par1Entity.posZ * 32D);
        encodedRotationYaw = MathHelper.floor_float((par1Entity.rotationYaw * 256F) / 360F);
        encodedRotationPitch = MathHelper.floor_float((par1Entity.rotationPitch * 256F) / 360F);
        field_48617_i = MathHelper.floor_float((par1Entity.func_48314_aq() * 256F) / 360F);
    }

    public boolean equals(Object par1Obj)
    {
        if (par1Obj instanceof EntityTrackerEntry)
        {
            return ((EntityTrackerEntry)par1Obj).trackedEntity.entityId == trackedEntity.entityId;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return trackedEntity.entityId;
    }

    public void updatePlayerList(List par1List)
    {
        playerEntitiesUpdated = false;

        if (!firstUpdateDone || trackedEntity.getDistanceSq(lastTrackedEntityPosX, lastTrackedEntityPosY, lastTrackedEntityPosZ) > 16D)
        {
            lastTrackedEntityPosX = trackedEntity.posX;
            lastTrackedEntityPosY = trackedEntity.posY;
            lastTrackedEntityPosZ = trackedEntity.posZ;
            firstUpdateDone = true;
            playerEntitiesUpdated = true;
            updatePlayerEntities(par1List);
        }

        field_28165_t++;

        if (updateCounter++ % field_9234_e == 0 || trackedEntity.isAirBorne)
        {
            //if (trackedEntity instanceof IClientDriven) //&& trackedEntity.riddenByEntity != null)
            if (trackedEntity instanceof IClientDriven && trackedEntity.riddenByEntity != null)
            {
                //sendPacketToTrackedPlayers(((IClientDriven) trackedEntity).getUpdatePacket());
                //Packet34EntityTeleport packet = new Packet34EntityTeleport(trackedEntity.entityId, encodedPosX, encodedPosY, encodedPosZ, (byte)encodedRotationYaw, (byte)encodedRotationPitch);

                Packet packet = ((IClientDriven) trackedEntity).getUpdatePacket();
                for (Object player : trackedPlayers)
                {
                    // send update packet to all clients except pilot
                    if (player.equals(trackedEntity.riddenByEntity)) continue;
                    ((EntityPlayerMP)player).playerNetServerHandler.sendPacket(packet);
                }


                // used by updatePlayerEntity for spawn/despawn trigger
                encodedPosX          = MathHelper.floor_double(trackedEntity.posX * 32D);
                encodedPosY          = MathHelper.floor_double(trackedEntity.posY * 32D);
                encodedPosZ          = MathHelper.floor_double(trackedEntity.posZ * 32D);
                encodedRotationYaw   = MathHelper.floor_float((trackedEntity.rotationYaw * 256F) / 360F);
                encodedRotationPitch = MathHelper.floor_float((trackedEntity.rotationPitch * 256F) / 360F);

                return;
            }



            int i = MathHelper.floor_double(trackedEntity.posX * 32D);
            int j = MathHelper.floor_double(trackedEntity.posY * 32D);
            int k = MathHelper.floor_double(trackedEntity.posZ * 32D);
            int l = MathHelper.floor_float((trackedEntity.rotationYaw * 256F) / 360F);
            int i1 = MathHelper.floor_float((trackedEntity.rotationPitch * 256F) / 360F);
            int j1 = i - encodedPosX;
            int k1 = j - encodedPosY;
            int l1 = k - encodedPosZ;
            Object obj = null;
            boolean flag = Math.abs(j1) >= 4 || Math.abs(k1) >= 4 || Math.abs(l1) >= 4;
            boolean flag1 = Math.abs(l - encodedRotationYaw) >= 4 || Math.abs(i1 - encodedRotationPitch) >= 4;

            if (j1 < -128 || j1 >= 128 || k1 < -128 || k1 >= 128 || l1 < -128 || l1 >= 128 || field_28165_t > 400)
            {
                field_28165_t = 0;
                trackedEntity.posX = (double)i / 32D;
                trackedEntity.posY = (double)j / 32D;
                trackedEntity.posZ = (double)k / 32D;
                obj = new Packet34EntityTeleport(trackedEntity.entityId, i, j, k, (byte)l, (byte)i1);
            }
            else if (flag && flag1)
            {
                obj = new Packet33RelEntityMoveLook(trackedEntity.entityId, (byte)j1, (byte)k1, (byte)l1, (byte)l, (byte)i1);
            }
            else if (flag)
            {
                obj = new Packet31RelEntityMove(trackedEntity.entityId, (byte)j1, (byte)k1, (byte)l1);
            }
            else if (flag1)
            {
                obj = new Packet32EntityLook(trackedEntity.entityId, (byte)l, (byte)i1);
            }

            if (shouldSendMotionUpdates)
            {
                double d = trackedEntity.motionX - lastTrackedEntityMotionX;
                double d1 = trackedEntity.motionY - lastTrackedEntityMotionY;
                double d2 = trackedEntity.motionZ - lastTrackedEntityMotionZ;
                double d3 = 0.02D;
                double d4 = d * d + d1 * d1 + d2 * d2;

                if (d4 > d3 * d3 || d4 > 0.0D && trackedEntity.motionX == 0.0D && trackedEntity.motionY == 0.0D && trackedEntity.motionZ == 0.0D)
                {
                    lastTrackedEntityMotionX = trackedEntity.motionX;
                    lastTrackedEntityMotionY = trackedEntity.motionY;
                    lastTrackedEntityMotionZ = trackedEntity.motionZ;
                    sendPacketToTrackedPlayers(new Packet28EntityVelocity(trackedEntity.entityId, lastTrackedEntityMotionX, lastTrackedEntityMotionY, lastTrackedEntityMotionZ));
                }
            }

            if (obj != null)
            {
                sendPacketToTrackedPlayers((Packet)obj);
            }

            DataWatcher datawatcher = trackedEntity.getDataWatcher();

            if (datawatcher.hasObjectChanged())
            {
                sendPacketToTrackedPlayersAndTrackedEntity(new Packet40EntityMetadata(trackedEntity.entityId, datawatcher));
            }

            int i2 = MathHelper.floor_float((trackedEntity.func_48314_aq() * 256F) / 360F);

            if (Math.abs(i2 - field_48617_i) >= 4)
            {
                sendPacketToTrackedPlayers(new Packet35EntityHeadRotation(trackedEntity.entityId, (byte)i2));
                field_48617_i = i2;
            }

            if (flag)
            {
                encodedPosX = i;
                encodedPosY = j;
                encodedPosZ = k;
            }

            if (flag1)
            {
                encodedRotationYaw = l;
                encodedRotationPitch = i1;
            }
        }

        trackedEntity.isAirBorne = false;

        if (trackedEntity.velocityChanged)
        {
            sendPacketToTrackedPlayersAndTrackedEntity(new Packet28EntityVelocity(trackedEntity));
            trackedEntity.velocityChanged = false;
        }
    }

    public void sendPacketToTrackedPlayers(Packet par1Packet)
    {
        EntityPlayerMP entityplayermp;

        for (Iterator iterator = trackedPlayers.iterator(); iterator.hasNext(); entityplayermp.playerNetServerHandler.sendPacket(par1Packet))
        {
            entityplayermp = (EntityPlayerMP)iterator.next();
        }
    }

    public void sendPacketToTrackedPlayersAndTrackedEntity(Packet par1Packet)
    {
        sendPacketToTrackedPlayers(par1Packet);

        if (trackedEntity instanceof EntityPlayerMP)
        {
            ((EntityPlayerMP)trackedEntity).playerNetServerHandler.sendPacket(par1Packet);
        }
    }

    public void sendDestroyEntityPacketToTrackedPlayers()
    {
        sendPacketToTrackedPlayers(new Packet29DestroyEntity(trackedEntity.entityId));
    }

    public void removeFromTrackedPlayers(EntityPlayerMP par1EntityPlayerMP)
    {
        if (trackedPlayers.contains(par1EntityPlayerMP))
        {
            trackedPlayers.remove(par1EntityPlayerMP);
        }
    }

    public void updatePlayerEntity(EntityPlayerMP par1EntityPlayerMP)
    {
        if (par1EntityPlayerMP == trackedEntity)
        {
            return;
        }

        double d = par1EntityPlayerMP.posX - (double)(encodedPosX / 32);
        double d1 = par1EntityPlayerMP.posZ - (double)(encodedPosZ / 32);

        if (d >= (double)(-trackingDistanceThreshold) && d <= (double)trackingDistanceThreshold && d1 >= (double)(-trackingDistanceThreshold) && d1 <= (double)trackingDistanceThreshold)
        {
            if (!trackedPlayers.contains(par1EntityPlayerMP))
            {
                trackedPlayers.add(par1EntityPlayerMP);
                par1EntityPlayerMP.playerNetServerHandler.sendPacket(getSpawnPacket());

                if (trackedEntity instanceof IClientDriven)
                {
                    System.out.println("ETE-THX: Adding player " + par1EntityPlayerMP.entityId + " to trackedPlayers list for trackedEntity " + trackedEntity.entityId);
                    return;
                }
                                                                                    

                if (shouldSendMotionUpdates)
                {
                    par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet28EntityVelocity(trackedEntity.entityId, trackedEntity.motionX, trackedEntity.motionY, trackedEntity.motionZ));
                }

                ItemStack aitemstack[] = trackedEntity.getInventory();

                if (aitemstack != null)
                {
                    for (int i = 0; i < aitemstack.length; i++)
                    {
                        par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet5PlayerInventory(trackedEntity.entityId, i, aitemstack[i]));
                    }
                }

                if (trackedEntity instanceof EntityPlayer)
                {
                    EntityPlayer entityplayer = (EntityPlayer)trackedEntity;

                    if (entityplayer.isPlayerSleeping())
                    {
                        par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet17Sleep(trackedEntity, 0, MathHelper.floor_double(trackedEntity.posX), MathHelper.floor_double(trackedEntity.posY), MathHelper.floor_double(trackedEntity.posZ)));
                    }
                }

                if (trackedEntity instanceof EntityLiving)
                {
                    EntityLiving entityliving = (EntityLiving)trackedEntity;
                    PotionEffect potioneffect;

                    for (Iterator iterator = entityliving.getActivePotionEffects().iterator(); iterator.hasNext(); par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet41EntityEffect(trackedEntity.entityId, potioneffect)))
                    {
                        potioneffect = (PotionEffect)iterator.next();
                    }
                }
            }
        }
        else if (trackedPlayers.contains(par1EntityPlayerMP))
        {
            trackedPlayers.remove(par1EntityPlayerMP);
            par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet29DestroyEntity(trackedEntity.entityId));
        }
    }

    public void updatePlayerEntities(List par1List)
    {
        for (int i = 0; i < par1List.size(); i++)
        {
            updatePlayerEntity((EntityPlayerMP)par1List.get(i));
        }
    }

    private Packet getSpawnPacket()
    {
        if (trackedEntity.isDead)
        {
            System.out.println("Fetching addPacket for removed entity");
        }

        EntityTrackerEntry2 entitytrackerentry2 = ModLoaderMp.handleEntityTrackerEntries(trackedEntity);

        if (entitytrackerentry2 != null)
        {
            try
            {
                if (trackedEntity instanceof ISpawnable)
                {
                    Packet230ModLoader packet230modloader = ((ISpawnable)trackedEntity).getSpawnPacket();
                    packet230modloader.modId = "Spawn".hashCode();

                    if (entitytrackerentry2.entityId > 127)
                    {
                        packet230modloader.packetType = entitytrackerentry2.entityId - 256;
                    }
                    else
                    {
                        packet230modloader.packetType = entitytrackerentry2.entityId;
                    }

                    return packet230modloader;
                }

                if (!entitytrackerentry2.entityHasOwner)
                {
                    return new Packet23VehicleSpawn(trackedEntity, entitytrackerentry2.entityId);
                }

                Field field = trackedEntity.getClass().getField("owner");

                if ((net.minecraft.src.Entity.class).isAssignableFrom(field.getType()))
                {
                    Entity entity1 = (Entity)field.get(trackedEntity);
                    return new Packet23VehicleSpawn(trackedEntity, entitytrackerentry2.entityId, entity1 == null ? trackedEntity.entityId : entity1.entityId);
                }
                else
                {
                    throw new Exception(String.format("Entity's owner field must be of type Entity, but it is of type %s.", new Object[]
                            {
                                field.getType()
                            }));
                }
            }
            catch (Exception exception)
            {
                ModLoader.getLogger().throwing("EntityTrackerEntry", "getSpawnPacket", exception);
                ModLoader.throwException(String.format("Error sending spawn packet for entity of type %s.", new Object[]
                        {
                            trackedEntity.getClass()
                        }), exception);
                return null;
            }
        }

        if (trackedEntity instanceof EntityItem)
        {
            EntityItem entityitem = (EntityItem)trackedEntity;
            Packet21PickupSpawn packet21pickupspawn = new Packet21PickupSpawn(entityitem);
            entityitem.posX = (double)packet21pickupspawn.xPosition / 32D;
            entityitem.posY = (double)packet21pickupspawn.yPosition / 32D;
            entityitem.posZ = (double)packet21pickupspawn.zPosition / 32D;
            return packet21pickupspawn;
        }

        if (trackedEntity instanceof EntityPlayerMP)
        {
            return new Packet20NamedEntitySpawn((EntityPlayer)trackedEntity);
        }

        if (trackedEntity instanceof EntityMinecart)
        {
            EntityMinecart entityminecart = (EntityMinecart)trackedEntity;

            if (entityminecart.minecartType == 0)
            {
                return new Packet23VehicleSpawn(trackedEntity, 10);
            }

            if (entityminecart.minecartType == 1)
            {
                return new Packet23VehicleSpawn(trackedEntity, 11);
            }

            if (entityminecart.minecartType == 2)
            {
                return new Packet23VehicleSpawn(trackedEntity, 12);
            }
        }

        if (trackedEntity instanceof EntityBoat)
        {
            return new Packet23VehicleSpawn(trackedEntity, 1);
        }

        if (trackedEntity instanceof IAnimals)
        {
            return new Packet24MobSpawn((EntityLiving)trackedEntity);
        }

        if (trackedEntity instanceof EntityDragon)
        {
            return new Packet24MobSpawn((EntityLiving)trackedEntity);
        }

        if (trackedEntity instanceof EntityFishHook)
        {
            return new Packet23VehicleSpawn(trackedEntity, 90);
        }

        if (trackedEntity instanceof EntityArrow)
        {
            Entity entity = ((EntityArrow)trackedEntity).shootingEntity;
            return new Packet23VehicleSpawn(trackedEntity, 60, entity != null ? entity.entityId : trackedEntity.entityId);
        }

        if (trackedEntity instanceof EntitySnowball)
        {
            return new Packet23VehicleSpawn(trackedEntity, 61);
        }

        if (trackedEntity instanceof EntityPotion)
        {
            return new Packet23VehicleSpawn(trackedEntity, 73, ((EntityPotion)trackedEntity).getPotionDamage());
        }

        if (trackedEntity instanceof EntityExpBottle)
        {
            return new Packet23VehicleSpawn(trackedEntity, 75);
        }

        if (trackedEntity instanceof EntityEnderPearl)
        {
            return new Packet23VehicleSpawn(trackedEntity, 65);
        }

        if (trackedEntity instanceof EntityEnderEye)
        {
            return new Packet23VehicleSpawn(trackedEntity, 72);
        }

        if (trackedEntity instanceof EntitySmallFireball)
        {
            EntitySmallFireball entitysmallfireball = (EntitySmallFireball)trackedEntity;
            Packet23VehicleSpawn packet23vehiclespawn = null;

            if (entitysmallfireball.shootingEntity != null)
            {
                packet23vehiclespawn = new Packet23VehicleSpawn(trackedEntity, 64, entitysmallfireball.shootingEntity.entityId);
            }
            else
            {
                packet23vehiclespawn = new Packet23VehicleSpawn(trackedEntity, 64, 0);
            }

            packet23vehiclespawn.speedX = (int)(entitysmallfireball.accelerationX * 8000D);
            packet23vehiclespawn.speedY = (int)(entitysmallfireball.accelerationY * 8000D);
            packet23vehiclespawn.speedZ = (int)(entitysmallfireball.accelerationZ * 8000D);
            return packet23vehiclespawn;
        }

        if (trackedEntity instanceof EntityFireball)
        {
            EntityFireball entityfireball = (EntityFireball)trackedEntity;
            Packet23VehicleSpawn packet23vehiclespawn1 = null;

            if (entityfireball.shootingEntity != null)
            {
                packet23vehiclespawn1 = new Packet23VehicleSpawn(trackedEntity, 63, ((EntityFireball)trackedEntity).shootingEntity.entityId);
            }
            else
            {
                packet23vehiclespawn1 = new Packet23VehicleSpawn(trackedEntity, 63, 0);
            }

            packet23vehiclespawn1.speedX = (int)(entityfireball.accelerationX * 8000D);
            packet23vehiclespawn1.speedY = (int)(entityfireball.accelerationY * 8000D);
            packet23vehiclespawn1.speedZ = (int)(entityfireball.accelerationZ * 8000D);
            return packet23vehiclespawn1;
        }

        if (trackedEntity instanceof EntityEgg)
        {
            return new Packet23VehicleSpawn(trackedEntity, 62);
        }

        if (trackedEntity instanceof EntityTNTPrimed)
        {
            return new Packet23VehicleSpawn(trackedEntity, 50);
        }

        if (trackedEntity instanceof EntityEnderCrystal)
        {
            return new Packet23VehicleSpawn(trackedEntity, 51);
        }

        if (trackedEntity instanceof EntityFallingSand)
        {
            EntityFallingSand entityfallingsand = (EntityFallingSand)trackedEntity;

            if (entityfallingsand.blockID == Block.sand.blockID)
            {
                return new Packet23VehicleSpawn(trackedEntity, 70);
            }

            if (entityfallingsand.blockID == Block.gravel.blockID)
            {
                return new Packet23VehicleSpawn(trackedEntity, 71);
            }

            if (entityfallingsand.blockID == Block.dragonEgg.blockID)
            {
                return new Packet23VehicleSpawn(trackedEntity, 74);
            }
        }

        if (trackedEntity instanceof EntityPainting)
        {
            return new Packet25EntityPainting((EntityPainting)trackedEntity);
        }

        if (trackedEntity instanceof EntityXPOrb)
        {
            return new Packet26EntityExpOrb((EntityXPOrb)trackedEntity);
        }
        else
        {
            throw new IllegalArgumentException((new StringBuilder()).append("Don't know how to add ").append(trackedEntity.getClass()).append("!").toString());
        }
    }

    /**
     * Remove a tracked player from our list and tell the tracked player to destroy us from their world.
     */
    public void removeTrackedPlayerSymmetric(EntityPlayerMP par1EntityPlayerMP)
    {
        if (trackedPlayers.contains(par1EntityPlayerMP))
        {
            trackedPlayers.remove(par1EntityPlayerMP);
            par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet29DestroyEntity(trackedEntity.entityId));
        }
    }
}
