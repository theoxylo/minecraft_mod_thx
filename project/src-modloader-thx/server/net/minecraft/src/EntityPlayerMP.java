package net.minecraft.src;

import java.util.*;
import net.minecraft.server.MinecraftServer;

public class EntityPlayerMP extends EntityPlayer
    implements ICrafting
{
    public NetServerHandler playerNetServerHandler;
    public MinecraftServer mcServer;
    public ItemInWorldManager itemInWorldManager;
    public double managedPosX;
    public double managedPosZ;
    public List loadedChunks;
    public Set listeningChunks;
    private int lastHealth;
    private int field_35221_cc;
    private boolean field_35222_cd;
    private int lastExperience;
    private int ticksOfInvuln;
    private ItemStack playerInventory[] =
    {
        null, null, null, null, null
    };
    private int currentWindowId;
    public boolean isChangingQuantityOnly;
    public int ping;
    public boolean gameOver;

    public EntityPlayerMP(MinecraftServer minecraftserver, World world, String s, ItemInWorldManager iteminworldmanager)
    {
        super(world);
        loadedChunks = new LinkedList();
        listeningChunks = new HashSet();
        lastHealth = 0xfa0a1f01;
        field_35221_cc = 0xfa0a1f01;
        field_35222_cd = true;
        lastExperience = 0xfa0a1f01;
        ticksOfInvuln = 60;
        currentWindowId = 0;
        gameOver = false;
        iteminworldmanager.thisPlayer = this;
        itemInWorldManager = iteminworldmanager;
        ChunkCoordinates chunkcoordinates = world.getSpawnPoint();
        int i = chunkcoordinates.posX;
        int j = chunkcoordinates.posZ;
        int k = chunkcoordinates.posY;
        if (!world.worldProvider.hasNoSky)
        {
            i += rand.nextInt(20) - 10;
            k = world.getTopSolidOrLiquidBlock(i, j);
            j += rand.nextInt(20) - 10;
        }
        setLocationAndAngles((double)i + 0.5D, k, (double)j + 0.5D, 0.0F, 0.0F);
        mcServer = minecraftserver;
        stepHeight = 0.0F;
        username = s;
        yOffset = 0.0F;
    }

    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey("playerGameType"))
        {
            itemInWorldManager.toggleGameType(nbttagcompound.getInteger("playerGameType"));
        }
    }

    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setInteger("playerGameType", itemInWorldManager.getGameType());
    }

    public void setWorld(World world)
    {
        super.setWorld(world);
    }

    public void removeExperience(int i)
    {
        super.removeExperience(i);
        lastExperience = -1;
    }

    public void func_20057_k()
    {
        craftingInventory.onCraftGuiOpened(this);
    }

    public ItemStack[] getInventory()
    {
        return playerInventory;
    }

    protected void resetHeight()
    {
        yOffset = 0.0F;
    }

    public float getEyeHeight()
    {
        return 1.62F;
    }

    public void onUpdate()
    {
        itemInWorldManager.updateBlockRemoving();
        ticksOfInvuln--;
        craftingInventory.updateCraftingResults();
        for (int i = 0; i < 5; i++)
        {
            ItemStack itemstack = getEquipmentInSlot(i);
            if (itemstack != playerInventory[i])
            {
                mcServer.getEntityTracker(dimension).sendPacketToTrackedPlayers(this, new Packet5PlayerInventory(entityId, i, itemstack));
                playerInventory[i] = itemstack;
            }
        }
    }

    public ItemStack getEquipmentInSlot(int i)
    {
        if (i == 0)
        {
            return inventory.getCurrentItem();
        }
        else
        {
            return inventory.armorInventory[i - 1];
        }
    }

    public void onDeath(DamageSource damagesource)
    {
        mcServer.configManager.sendPacketToAllPlayers(new Packet3Chat(damagesource.func_35075_a(this)));
        inventory.dropAllItems();
    }

    public boolean attackEntityFrom(DamageSource damagesource, int i)
    {
        if (ticksOfInvuln > 0)
        {
            return false;
        }
        if (!mcServer.pvpOn && (damagesource instanceof EntityDamageSource))
        {
            Entity entity = damagesource.getEntity();
            if (entity instanceof EntityPlayer)
            {
                return false;
            }
            if (entity instanceof EntityArrow)
            {
                EntityArrow entityarrow = (EntityArrow)entity;
                if (entityarrow.shootingEntity instanceof EntityPlayer)
                {
                    return false;
                }
            }
        }
        return super.attackEntityFrom(damagesource, i);
    }

    protected boolean isPVPEnabled()
    {
        return mcServer.pvpOn;
    }

    public void heal(int i)
    {
        super.heal(i);
    }

    public void onUpdateEntity(boolean flag)
    {
        super.onUpdate();
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if (itemstack == null || !Item.itemsList[itemstack.itemID].func_28019_b() || playerNetServerHandler.getNumChunkDataPackets() > 2)
            {
                continue;
            }
            Packet packet = ((ItemMapBase)Item.itemsList[itemstack.itemID]).getUpdatePacket(itemstack, worldObj, this);
            if (packet != null)
            {
                playerNetServerHandler.sendPacket(packet);
            }
        }

        if (flag && !loadedChunks.isEmpty())
        {
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)loadedChunks.get(0);
            if (chunkcoordintpair != null)
            {
                boolean flag1 = false;
                if (playerNetServerHandler.getNumChunkDataPackets() < 4)
                {
                    flag1 = true;
                }
                if (flag1)
                {
                    WorldServer worldserver = mcServer.getWorldManager(dimension);
                    loadedChunks.remove(chunkcoordintpair);
                    playerNetServerHandler.sendPacket(new Packet51MapChunk(chunkcoordintpair.chunkXPos * 16, 0, chunkcoordintpair.chunkZPos * 16, 16, worldserver.worldHeight, 16, worldserver));
                    List list = worldserver.getTileEntityList(chunkcoordintpair.chunkXPos * 16, 0, chunkcoordintpair.chunkZPos * 16, chunkcoordintpair.chunkXPos * 16 + 16, worldserver.worldHeight, chunkcoordintpair.chunkZPos * 16 + 16);
                    for (int j = 0; j < list.size(); j++)
                    {
                        getTileEntityInfo((TileEntity)list.get(j));
                    }
                }
            }
        }
        if (inPortal)
        {
            if (mcServer.propertyManagerObj.getBooleanProperty("allow-nether", true))
            {
                if (craftingInventory != inventorySlots)
                {
                    closeScreen();
                }
                if (ridingEntity != null)
                {
                    mountEntity(ridingEntity);
                }
                else
                {
                    timeInPortal += 0.0125F;
                    if (timeInPortal >= 1.0F)
                    {
                        timeInPortal = 1.0F;
                        timeUntilPortal = 10;
                        byte byte0 = 0;
                        if (dimension == -1)
                        {
                            byte0 = 0;
                        }
                        else
                        {
                            byte0 = -1;
                        }
                        mcServer.configManager.sendPlayerToOtherDimension(this, byte0);
                        lastExperience = -1;
                        lastHealth = -1;
                        field_35221_cc = -1;
                        triggerAchievement(AchievementList.portal);
                    }
                }
                inPortal = false;
            }
        }
        else
        {
            if (timeInPortal > 0.0F)
            {
                timeInPortal -= 0.05F;
            }
            if (timeInPortal < 0.0F)
            {
                timeInPortal = 0.0F;
            }
        }
        if (timeUntilPortal > 0)
        {
            timeUntilPortal--;
        }
        if (getEntityHealth() != lastHealth || field_35221_cc != foodStats.getFoodLevel() || (foodStats.getSaturationLevel() == 0.0F) != field_35222_cd)
        {
            playerNetServerHandler.sendPacket(new Packet8UpdateHealth(getEntityHealth(), foodStats.getFoodLevel(), foodStats.getSaturationLevel()));
            lastHealth = getEntityHealth();
            field_35221_cc = foodStats.getFoodLevel();
            field_35222_cd = foodStats.getSaturationLevel() == 0.0F;
        }
        if (experienceTotal != lastExperience)
        {
            lastExperience = experienceTotal;
            playerNetServerHandler.sendPacket(new Packet43Experience(experience, experienceTotal, experienceLevel));
        }
    }

    public void travelToTheEnd(int i)
    {
        if (dimension == 1 && i == 1)
        {
            triggerAchievement(AchievementList.theEnd2);
            worldObj.setEntityDead(this);
            gameOver = true;
            playerNetServerHandler.sendPacket(new Packet70Bed(4, 0));
        }
        else
        {
            triggerAchievement(AchievementList.theEnd);
            ChunkCoordinates chunkcoordinates = mcServer.getWorldManager(i).getEntrancePortalLocation();
            if (chunkcoordinates != null)
            {
                playerNetServerHandler.teleportTo(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, 0.0F, 0.0F);
            }
            mcServer.configManager.sendPlayerToOtherDimension(this, 1);
            lastExperience = -1;
            lastHealth = -1;
            field_35221_cc = -1;
        }
    }

    private void getTileEntityInfo(TileEntity tileentity)
    {
        if (tileentity != null)
        {
            Packet packet = tileentity.getDescriptionPacket();
            if (packet != null)
            {
                playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    public void onItemPickup(Entity entity, int i)
    {
        if (!entity.isDead)
        {
            EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
            if (entity instanceof EntityItem)
            {
                entitytracker.sendPacketToTrackedPlayers(entity, new Packet22Collect(entity.entityId, entityId));
            }
            if (entity instanceof EntityArrow)
            {
                entitytracker.sendPacketToTrackedPlayers(entity, new Packet22Collect(entity.entityId, entityId));
            }
            if (entity instanceof EntityXPOrb)
            {
                entitytracker.sendPacketToTrackedPlayers(entity, new Packet22Collect(entity.entityId, entityId));
            }
        }
        super.onItemPickup(entity, i);
        craftingInventory.updateCraftingResults();
    }

    public void swingItem()
    {
        if (!isSwinging)
        {
            swingProgressInt = -1;
            isSwinging = true;
            EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
            entitytracker.sendPacketToTrackedPlayers(this, new Packet18Animation(this, 1));
        }
    }

    public void func_22068_s()
    {
    }

    public EnumStatus sleepInBedAt(int i, int j, int k)
    {
        EnumStatus enumstatus = super.sleepInBedAt(i, j, k);
        if (enumstatus == EnumStatus.OK)
        {
            EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
            Packet17Sleep packet17sleep = new Packet17Sleep(this, 0, i, j, k);
            entitytracker.sendPacketToTrackedPlayers(this, packet17sleep);
            playerNetServerHandler.teleportTo(posX, posY, posZ, rotationYaw, rotationPitch);
            playerNetServerHandler.sendPacket(packet17sleep);
        }
        return enumstatus;
    }

    public void wakeUpPlayer(boolean flag, boolean flag1, boolean flag2)
    {
        if (isPlayerSleeping())
        {
            EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
            entitytracker.sendPacketToTrackedPlayersAndTrackedEntity(this, new Packet18Animation(this, 3));
        }
        super.wakeUpPlayer(flag, flag1, flag2);
        if (playerNetServerHandler != null)
        {
            playerNetServerHandler.teleportTo(posX, posY, posZ, rotationYaw, rotationPitch);
        }
    }

    public void mountEntity(Entity entity)
    {
        System.out.println("EntityPlayerMP.mountEntity called with entity " + entity.entityId);
        
        super.mountEntity(entity);
        playerNetServerHandler.sendPacket(new Packet39AttachEntity(this, ridingEntity));
        playerNetServerHandler.teleportTo(posX, posY, posZ, rotationYaw, rotationPitch);
    }

    protected void updateFallState(double d, boolean flag)
    {
    }

    public void handleFalling(double d, boolean flag)
    {
        super.updateFallState(d, flag);
    }

    private void getNextWidowId()
    {
        currentWindowId = currentWindowId % 100 + 1;
    }

    public void displayWorkbenchGUI(int i, int j, int k)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 1, "Crafting", 9));
        craftingInventory = new ContainerWorkbench(inventory, worldObj, i, j, k);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    public void displayGUIEnchantment(int i, int j, int k)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 4, "Enchanting", 9));
        craftingInventory = new ContainerEnchantment(inventory, worldObj, i, j, k);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    public void displayGUIChest(IInventory iinventory)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 0, iinventory.getInvName(), iinventory.getSizeInventory()));
        craftingInventory = new ContainerChest(inventory, iinventory);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    public void displayGUIFurnace(TileEntityFurnace tileentityfurnace)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 2, tileentityfurnace.getInvName(), tileentityfurnace.getSizeInventory()));
        craftingInventory = new ContainerFurnace(inventory, tileentityfurnace);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    public void displayGUIDispenser(TileEntityDispenser tileentitydispenser)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 3, tileentitydispenser.getInvName(), tileentitydispenser.getSizeInventory()));
        craftingInventory = new ContainerDispenser(inventory, tileentitydispenser);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    public void displayGUIBrewingStand(TileEntityBrewingStand tileentitybrewingstand)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 5, tileentitybrewingstand.getInvName(), tileentitybrewingstand.getSizeInventory()));
        craftingInventory = new ContainerBrewingStand(inventory, tileentitybrewingstand);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    public void updateCraftingInventorySlot(Container container, int i, ItemStack itemstack)
    {
        if (container.getSlot(i) instanceof SlotCrafting)
        {
            return;
        }
        if (isChangingQuantityOnly)
        {
            return;
        }
        else
        {
            playerNetServerHandler.sendPacket(new Packet103SetSlot(container.windowId, i, itemstack));
            return;
        }
    }

    public void func_28017_a(Container container)
    {
        updateCraftingInventory(container, container.func_28127_b());
    }

    public void updateCraftingInventory(Container container, List list)
    {
        playerNetServerHandler.sendPacket(new Packet104WindowItems(container.windowId, list));
        playerNetServerHandler.sendPacket(new Packet103SetSlot(-1, -1, inventory.getItemStack()));
    }

    public void updateCraftingInventoryInfo(Container container, int i, int j)
    {
        playerNetServerHandler.sendPacket(new Packet105UpdateProgressbar(container.windowId, i, j));
    }

    public void onItemStackChanged(ItemStack itemstack)
    {
    }

    public void closeScreen()
    {
        playerNetServerHandler.sendPacket(new Packet101CloseWindow(craftingInventory.windowId));
        closeCraftingGui();
    }

    public void updateHeldItem()
    {
        if (isChangingQuantityOnly)
        {
            return;
        }
        else
        {
            playerNetServerHandler.sendPacket(new Packet103SetSlot(-1, -1, inventory.getItemStack()));
            return;
        }
    }

    public void closeCraftingGui()
    {
        craftingInventory.onCraftGuiClosed(this);
        craftingInventory = inventorySlots;
    }

    public void addStat(StatBase statbase, int i)
    {
        if (statbase == null)
        {
            return;
        }
        if (!statbase.isIndependent)
        {
            for (; i > 100; i -= 100)
            {
                playerNetServerHandler.sendPacket(new Packet200Statistic(statbase.statId, 100));
            }

            playerNetServerHandler.sendPacket(new Packet200Statistic(statbase.statId, i));
        }
    }

    public void func_30002_A()
    {
        if (ridingEntity != null)
        {
            mountEntity(ridingEntity);
        }
        if (riddenByEntity != null)
        {
            riddenByEntity.mountEntity(this);
        }
        if (sleeping)
        {
            wakeUpPlayer(true, false, false);
        }
    }

    public void func_30001_B()
    {
        lastHealth = 0xfa0a1f01;
    }

    public void addChatMessage(String s)
    {
        StringTranslate stringtranslate = StringTranslate.getInstance();
        String s1 = stringtranslate.translateKey(s);
        playerNetServerHandler.sendPacket(new Packet3Chat(s1));
    }

    protected void func_35199_C()
    {
        playerNetServerHandler.sendPacket(new Packet38EntityStatus(entityId, (byte)9));
        super.func_35199_C();
    }

    public void setItemInUse(ItemStack itemstack, int i)
    {
        super.setItemInUse(itemstack, i);
        if (itemstack != null && itemstack.getItem() != null && itemstack.getItem().getItemUseAction(itemstack) == EnumAction.eat)
        {
            EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
            entitytracker.sendPacketToTrackedPlayersAndTrackedEntity(this, new Packet18Animation(this, 5));
        }
    }

    protected void onNewPotionEffect(PotionEffect potioneffect)
    {
        super.onNewPotionEffect(potioneffect);
        playerNetServerHandler.sendPacket(new Packet41EntityEffect(entityId, potioneffect));
    }

    protected void onChangedPotionEffect(PotionEffect potioneffect)
    {
        super.onChangedPotionEffect(potioneffect);
        playerNetServerHandler.sendPacket(new Packet41EntityEffect(entityId, potioneffect));
    }

    protected void onFinishedPotionEffect(PotionEffect potioneffect)
    {
        super.onFinishedPotionEffect(potioneffect);
        playerNetServerHandler.sendPacket(new Packet42RemoveEntityEffect(entityId, potioneffect));
    }

    public void setPositionAndUpdate(double d, double d1, double d2)
    {
        playerNetServerHandler.teleportTo(d, d1, d2, rotationYaw, rotationPitch);
    }

    public void onCriticalHit(Entity entity)
    {
        EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
        entitytracker.sendPacketToTrackedPlayersAndTrackedEntity(this, new Packet18Animation(entity, 6));
    }

    public void onEnchantmentCritical(Entity entity)
    {
        EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
        entitytracker.sendPacketToTrackedPlayersAndTrackedEntity(this, new Packet18Animation(entity, 7));
    }
}
