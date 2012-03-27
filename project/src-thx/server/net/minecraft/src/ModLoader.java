package net.minecraft.src;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.minecraft.server.MinecraftServer;

public final class ModLoader
{
    private static File cfgdir;
    private static File cfgfile;
    public static Level cfgLoggingLevel;
    private static long clock = 0L;
    public static final boolean DEBUG = false;
    private static Field field_modifiers = null;
    private static Map classMap = null;
    private static boolean hasInit = false;
    private static int highestEntityId = 3000;
    private static final Map inGameHooks = new HashMap();
    private static MinecraftServer instance = null;
    private static int itemSpriteIndex = 0;
    private static int itemSpritesLeft = 0;
    private static File logfile;
    private static File modDir;
    private static final Logger logger = Logger.getLogger("ModLoader");
    private static FileHandler logHandler = null;
    private static Method method_RegisterEntityID = null;
    private static Method method_RegisterTileEntity = null;
    private static final LinkedList modList = new LinkedList();
    private static int nextBlockModelID = 1000;
    private static final Map overrides = new HashMap();
    public static final Properties props = new Properties();
    private static BiomeGenBase standardBiomes[];
    private static int terrainSpriteIndex = 0;
    private static int terrainSpritesLeft = 0;
    private static final boolean usedItemSprites[] = new boolean[256];
    private static final boolean usedTerrainSprites[] = new boolean[256];
    public static final String VERSION = "ModLoader Server 1.2.3v3";
    private static Method method_getNextWindowId;
    private static Field field_currentWindowId;

    public ModLoader()
    {
    }

    public static void addAchievementDesc(Achievement achievement, String s, String s1)
    {
        try
        {
            if (achievement.toString().contains("."))
            {
                String as[] = achievement.toString().split("\\.");

                if (as.length == 2)
                {
                    String s2 = as[1];
                    setPrivateValue(net.minecraft.src.StatBase.class, achievement, 1, StatCollector.translateToLocal((new StringBuilder()).append("achievement.").append(s2).toString()));
                    setPrivateValue(net.minecraft.src.Achievement.class, achievement, 3, StatCollector.translateToLocal((new StringBuilder()).append("achievement.").append(s2).append(".desc").toString()));
                }
                else
                {
                    setPrivateValue(net.minecraft.src.StatBase.class, achievement, 1, s);
                    setPrivateValue(net.minecraft.src.Achievement.class, achievement, 3, s1);
                }
            }
            else
            {
                setPrivateValue(net.minecraft.src.StatBase.class, achievement, 1, s);
                setPrivateValue(net.minecraft.src.Achievement.class, achievement, 3, s1);
            }
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            logger.throwing("ModLoader", "AddAchievementDesc", illegalargumentexception);
            throwException(illegalargumentexception);
        }
        catch (SecurityException securityexception)
        {
            logger.throwing("ModLoader", "AddAchievementDesc", securityexception);
            throwException(securityexception);
        }
        catch (NoSuchFieldException nosuchfieldexception)
        {
            logger.throwing("ModLoader", "AddAchievementDesc", nosuchfieldexception);
            throwException(nosuchfieldexception);
        }
    }

    public static int addAllFuel(int i, int j)
    {
        logger.finest((new StringBuilder()).append("Finding fuel for ").append(i).toString());
        int k = 0;

        for (Iterator iterator = modList.iterator(); iterator.hasNext() && k == 0; k = ((BaseMod)iterator.next()).addFuel(i, j)) { }

        if (k != 0)
        {
            logger.finest((new StringBuilder()).append("Returned ").append(k).toString());
        }

        return k;
    }

    public static int addArmor(String s)
    {
        return -1;
    }

    private static void addMod(ClassLoader classloader, String s)
    {
        try
        {
            String s1 = s.split("\\.")[0];

            if (s1.contains("$"))
            {
                return;
            }

            if (props.containsKey(s1) && (props.getProperty(s1).equalsIgnoreCase("no") || props.getProperty(s1).equalsIgnoreCase("off")))
            {
                return;
            }

            Package package1 = (net.minecraft.src.ModLoader.class).getPackage();

            if (package1 != null)
            {
                s1 = (new StringBuilder()).append(package1.getName()).append(".").append(s1).toString();
            }

            Class class1 = classloader.loadClass(s1);

            if (!(net.minecraft.src.BaseMod.class).isAssignableFrom(class1))
            {
                return;
            }

            setupProperties(class1);
            BaseMod basemod = (BaseMod)class1.newInstance();

            if (basemod != null)
            {
                modList.add(basemod);
                logger.fine((new StringBuilder()).append("Mod Initialized: \"").append(basemod.toString()).append("\" from ").append(s).toString());
                System.out.println((new StringBuilder()).append("Mod Initialized: ").append(basemod.toString()).toString());
                MinecraftServer.logger.info((new StringBuilder()).append("Mod Initialized: ").append(basemod.toString()).toString());
            }
        }
        catch (Throwable throwable)
        {
            logger.fine((new StringBuilder()).append("Failed to load mod from \"").append(s).append("\"").toString());
            System.out.println((new StringBuilder()).append("Failed to load mod from \"").append(s).append("\"").toString());
            logger.throwing("ModLoader", "addMod", throwable);
            throwException(throwable);
        }
    }

    public static int addOverride(String s, String s1)
    {
        return 0;
    }

    public static void addOverride(String s, String s1, int i)
    {
        boolean flag = true;
        boolean flag1 = false;
        int j;
        int k;

        if (s.equals("/terrain.png"))
        {
            j = 0;
            k = terrainSpritesLeft;
        }
        else
        {
            if (!s.equals("/gui/items.png"))
            {
                return;
            }

            j = 1;
            k = itemSpritesLeft;
        }

        System.out.println((new StringBuilder()).append("Overriding ").append(s).append(" with ").append(s1).append(" @ ").append(i).append(". ").append(k).append(" left.").toString());
        logger.finer((new StringBuilder()).append("addOverride(").append(s).append(",").append(s1).append(",").append(i).append("). ").append(k).append(" left.").toString());
        Object obj = (Map)overrides.get(Integer.valueOf(j));

        if (obj == null)
        {
            obj = new HashMap();
            overrides.put(Integer.valueOf(j), obj);
        }

        ((Map)obj).put(s1, Integer.valueOf(i));
    }

    public static void addRecipe(ItemStack itemstack, Object aobj[])
    {
        CraftingManager.getInstance().addRecipe(itemstack, aobj);
    }

    public static void addShapelessRecipe(ItemStack itemstack, Object aobj[])
    {
        CraftingManager.getInstance().addShapelessRecipe(itemstack, aobj);
    }

    public static void addSmelting(int i, ItemStack itemstack)
    {
        FurnaceRecipes.smelting().addSmelting(i, itemstack);
    }

    public static void addSpawn(Class class1, int i, int j, int k, EnumCreatureType enumcreaturetype)
    {
        addSpawn(class1, i, j, k, enumcreaturetype, (BiomeGenBase[])null);
    }

    public static void addSpawn(Class class1, int i, int j, int k, EnumCreatureType enumcreaturetype, BiomeGenBase abiomegenbase[])
    {
        if (class1 == null)
        {
            throw new IllegalArgumentException("entityClass cannot be null");
        }

        if (enumcreaturetype == null)
        {
            throw new IllegalArgumentException("spawnList cannot be null");
        }

        if (abiomegenbase == null)
        {
            abiomegenbase = standardBiomes;
        }

        for (int l = 0; l < abiomegenbase.length; l++)
        {
            List list = abiomegenbase[l].getSpawnableList(enumcreaturetype);

            if (list == null)
            {
                continue;
            }

            boolean flag = false;
            Iterator iterator = list.iterator();

            do
            {
                if (!iterator.hasNext())
                {
                    break;
                }

                SpawnListEntry spawnlistentry = (SpawnListEntry)iterator.next();

                if (spawnlistentry.entityClass != class1)
                {
                    continue;
                }

                spawnlistentry.itemWeight = i;
                spawnlistentry.minGroupCount = j;
                spawnlistentry.maxGroupCount = k;
                flag = true;
                break;
            }
            while (true);

            if (!flag)
            {
                list.add(new SpawnListEntry(class1, i, j, k));
            }
        }
    }

    public static void addSpawn(String s, int i, int j, int k, EnumCreatureType enumcreaturetype)
    {
        addSpawn(s, i, j, k, enumcreaturetype, (BiomeGenBase[])null);
    }

    public static void addSpawn(String s, int i, int j, int k, EnumCreatureType enumcreaturetype, BiomeGenBase abiomegenbase[])
    {
        Class class1 = (Class)classMap.get(s);

        if (class1 != null && (net.minecraft.src.EntityLiving.class).isAssignableFrom(class1))
        {
            addSpawn(class1, i, j, k, enumcreaturetype, abiomegenbase);
        }
    }

    public static boolean dispenseEntity(World world, double d, double d1, double d2, int i, int j, ItemStack itemstack)
    {
        boolean flag = false;

        for (Iterator iterator = modList.iterator(); iterator.hasNext() && !flag; flag = ((BaseMod)iterator.next()).dispenseEntity(world, d, d1, d2, i, j, itemstack)) { }

        return flag;
    }

    public static void genericContainerRemoval(World world, int i, int j, int k)
    {
        IInventory iinventory = (IInventory)world.getBlockTileEntity(i, j, k);

        if (iinventory != null)
        {
            for (int l = 0; l < iinventory.getSizeInventory(); l++)
            {
                ItemStack itemstack = iinventory.getStackInSlot(l);

                if (itemstack == null)
                {
                    continue;
                }

                double d = world.rand.nextDouble() * 0.80000000000000004D + 0.10000000000000001D;
                double d1 = world.rand.nextDouble() * 0.80000000000000004D + 0.10000000000000001D;
                double d2 = world.rand.nextDouble() * 0.80000000000000004D + 0.10000000000000001D;

                while (itemstack.stackSize > 0)
                {
                    int i1 = world.rand.nextInt(21) + 10;

                    if (i1 > itemstack.stackSize)
                    {
                        i1 = itemstack.stackSize;
                    }

                    itemstack.stackSize -= i1;
                    EntityItem entityitem = new EntityItem(world, (double)i + d, (double)j + d1, (double)k + d2, new ItemStack(itemstack.itemID, i1, itemstack.getItemDamage()));
                    double d3 = 0.050000000000000003D;
                    entityitem.motionX = world.rand.nextGaussian() * d3;
                    entityitem.motionY = world.rand.nextGaussian() * d3 + 0.20000000000000001D;
                    entityitem.motionZ = world.rand.nextGaussian() * d3;

                    if (itemstack.hasTagCompound())
                    {
                        entityitem.item.setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                    }

                    world.spawnEntityInWorld(entityitem);
                }

                iinventory.setInventorySlotContents(l, (ItemStack)null);
            }
        }
    }

    public static List getLoadedMods()
    {
        return Collections.unmodifiableList(modList);
    }

    public static Logger getLogger()
    {
        return logger;
    }

    public static MinecraftServer getMinecraftServerInstance()
    {
        return instance;
    }

    public static Object getPrivateValue(Class class1, Object obj, int i) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field field = class1.getDeclaredFields()[i];
            field.setAccessible(true);
            return field.get(obj);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "getPrivateValue", illegalaccessexception);
            throwException("An impossible error has occured!", illegalaccessexception);
            return null;
        }
    }

    public static Object getPrivateValue(Class class1, Object obj, String s) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field field = class1.getDeclaredField(s);
            field.setAccessible(true);
            return field.get(obj);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "getPrivateValue", illegalaccessexception);
            throwException("An impossible error has occured!", illegalaccessexception);
            return null;
        }
    }

    public static int getUniqueBlockModelID(BaseMod basemod, boolean flag)
    {
        return nextBlockModelID++;
    }

    public static int getUniqueEntityId()
    {
        return highestEntityId++;
    }

    private static int getUniqueItemSpriteIndex()
    {
        for (; itemSpriteIndex < usedItemSprites.length; itemSpriteIndex++)
        {
            if (!usedItemSprites[itemSpriteIndex])
            {
                usedItemSprites[itemSpriteIndex] = true;
                itemSpritesLeft--;
                return itemSpriteIndex++;
            }
        }

        return itemSpriteIndex++;
    }

    public static int getUniqueSpriteIndex(String s)
    {
        if (s.equals("/gui/items.png"))
        {
            return getUniqueItemSpriteIndex();
        }

        if (s.equals("/terrain.png"))
        {
            return getUniqueTerrainSpriteIndex();
        }
        else
        {
            Exception exception = new Exception((new StringBuilder()).append("No registry for this texture: ").append(s).toString());
            logger.throwing("ModLoader", "getUniqueItemSpriteIndex", exception);
            throwException(exception);
            return 0;
        }
    }

    private static int getUniqueTerrainSpriteIndex()
    {
        for (; terrainSpriteIndex < usedTerrainSprites.length; terrainSpriteIndex++)
        {
            if (!usedTerrainSprites[terrainSpriteIndex])
            {
                usedTerrainSprites[terrainSpriteIndex] = true;
                terrainSpritesLeft--;
                return terrainSpriteIndex++;
            }
        }

        return terrainSpriteIndex++;
    }

    private static void init()
    {
        hasInit = true;
        String s = "1111111111111111111111111111111111111101111111111111111111111111111111111111111111111111111111111111110111111111111111000111111111111101111111110000000101111111000000010101111100000000000000110000000000000000000000000000000000000000000000001111111111111111";
        String s1 = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111110000001111111111000000001111110000000111111111000000001111111110000001111111111111111111";

        for (int i = 0; i < 256; i++)
        {
            usedItemSprites[i] = s.charAt(i) == '1';

            if (!usedItemSprites[i])
            {
                itemSpritesLeft++;
            }

            usedTerrainSprites[i] = s1.charAt(i) == '1';

            if (!usedTerrainSprites[i])
            {
                terrainSpritesLeft++;
            }
        }

        try
        {
            classMap = (Map)getPrivateValue(net.minecraft.src.EntityList.class, (Object)null, 0);
            field_modifiers = (java.lang.reflect.Field.class).getDeclaredField("modifiers");
            field_modifiers.setAccessible(true);
            Field afield[] = (net.minecraft.src.BiomeGenBase.class).getDeclaredFields();
            LinkedList linkedlist = new LinkedList();
            int j = 0;

            do
            {
                if (j >= afield.length)
                {
                    standardBiomes = (BiomeGenBase[])linkedlist.toArray(new BiomeGenBase[0]);

                    try
                    {
                        method_RegisterTileEntity = (net.minecraft.src.TileEntity.class).getDeclaredMethod("a", new Class[]
                                {
                                    java.lang.Class.class, java.lang.String.class
                                });
                    }
                    catch (NoSuchMethodException nosuchmethodexception1)
                    {
                        method_RegisterTileEntity = (net.minecraft.src.TileEntity.class).getDeclaredMethod("addMapping", new Class[]
                                {
                                    java.lang.Class.class, java.lang.String.class
                                });
                    }

                    method_RegisterTileEntity.setAccessible(true);

                    try
                    {
                        method_RegisterEntityID = (net.minecraft.src.EntityList.class).getDeclaredMethod("a", new Class[]
                                {
                                    java.lang.Class.class, java.lang.String.class, Integer.TYPE
                                });
                    }
                    catch (NoSuchMethodException nosuchmethodexception2)
                    {
                        method_RegisterEntityID = (net.minecraft.src.EntityList.class).getDeclaredMethod("addMapping", new Class[]
                                {
                                    java.lang.Class.class, java.lang.String.class, Integer.TYPE
                                });
                    }

                    method_RegisterEntityID.setAccessible(true);
                    break;
                }

                Class class1 = afield[j].getType();

                if ((afield[j].getModifiers() & 8) != 0 && class1.isAssignableFrom(net.minecraft.src.BiomeGenBase.class))
                {
                    BiomeGenBase biomegenbase = (BiomeGenBase)afield[j].get((Object)null);

                    if (!(biomegenbase instanceof BiomeGenHell) && !(biomegenbase instanceof BiomeGenEnd))
                    {
                        linkedlist.add(biomegenbase);
                    }
                }

                j++;
            }
            while (true);
        }
        catch (SecurityException securityexception)
        {
            logger.throwing("ModLoader", "init", securityexception);
            throwException(securityexception);
            throw new RuntimeException(securityexception);
        }
        catch (NoSuchFieldException nosuchfieldexception)
        {
            logger.throwing("ModLoader", "init", nosuchfieldexception);
            throwException(nosuchfieldexception);
            throw new RuntimeException(nosuchfieldexception);
        }
        catch (NoSuchMethodException nosuchmethodexception)
        {
            logger.throwing("ModLoader", "init", nosuchmethodexception);
            throwException(nosuchmethodexception);
            throw new RuntimeException(nosuchmethodexception);
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            logger.throwing("ModLoader", "init", illegalargumentexception);
            throwException(illegalargumentexception);
            throw new RuntimeException(illegalargumentexception);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "init", illegalaccessexception);
            throwException(illegalaccessexception);
            throw new RuntimeException(illegalaccessexception);
        }

        try
        {
            loadConfig();

            if (props.containsKey("loggingLevel"))
            {
                cfgLoggingLevel = Level.parse(props.getProperty("loggingLevel"));
            }

            logger.setLevel(cfgLoggingLevel);

            if ((logfile.exists() || logfile.createNewFile()) && logfile.canWrite() && logHandler == null)
            {
                logHandler = new FileHandler(logfile.getPath());
                logHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(logHandler);
            }

            logger.fine("ModLoader Server 1.2.3v3 Initializing...");
            System.out.println("ModLoader Server 1.2.3v3 Initializing...");
            MinecraftServer.logger.info("ModLoader Server 1.2.3v3 Initializing...");
            File file = new File((net.minecraft.src.ModLoader.class).getProtectionDomain().getCodeSource().getLocation().toURI());
            modDir.mkdirs();
            readFromClassPath(file);
            readFromModFolder(modDir);
            sortModList();
            Iterator iterator = modList.iterator();

            do
            {
                if (!iterator.hasNext())
                {
                    break;
                }

                BaseMod basemod = (BaseMod)iterator.next();
                basemod.load();
                logger.fine((new StringBuilder()).append("Mod Loaded: \"").append(basemod.toString()).append("\"").toString());
                System.out.println((new StringBuilder()).append("Mod Loaded: ").append(basemod.toString()).toString());

                if (!props.containsKey(basemod.getClass().getSimpleName()))
                {
                    props.setProperty(basemod.getClass().getSimpleName(), "on");
                }
            }
            while (true);

            BaseMod basemod1;

            for (Iterator iterator1 = modList.iterator(); iterator1.hasNext(); basemod1.modsLoaded())
            {
                basemod1 = (BaseMod)iterator1.next();
            }

            System.out.println("Done.");
            props.setProperty("loggingLevel", cfgLoggingLevel.getName());
            initStats();
            saveConfig();
        }
        catch (Throwable throwable)
        {
            logger.throwing("ModLoader", "init", throwable);
            throwException("ModLoader has failed to initialize.", throwable);

            if (logHandler != null)
            {
                logHandler.close();
            }

            throw new RuntimeException(throwable);
        }
    }

    private static void initStats()
    {
        for (int i = 0; i < Block.blocksList.length; i++)
        {
            if (!StatList.oneShotStats.containsKey(Integer.valueOf(0x1000000 + i)) && Block.blocksList[i] != null && Block.blocksList[i].getEnableStats())
            {
                String s = StringTranslate.getInstance().translateKeyFormat("stat.mineBlock", new Object[]
                        {
                            Boolean.valueOf(Block.blocksList[i].getTickRandomly())
                        });
                StatList.mineBlockStatArray[i] = (new StatCrafting(0x1000000 + i, s, i)).registerStat();
                StatList.objectMineStats.add(StatList.mineBlockStatArray[i]);
            }
        }

        for (int j = 0; j < Item.itemsList.length; j++)
        {
            if (!StatList.oneShotStats.containsKey(Integer.valueOf(0x1020000 + j)) && Item.itemsList[j] != null)
            {
                String s1 = StringTranslate.getInstance().translateKeyFormat("stat.useItem", new Object[]
                        {
                            Item.itemsList[j].getStatName()
                        });
                StatList.objectUseStats[j] = (new StatCrafting(0x1020000 + j, s1, j)).registerStat();

                if (j >= Block.blocksList.length)
                {
                    StatList.itemStats.add(StatList.objectUseStats[j]);
                }
            }

            if (!StatList.oneShotStats.containsKey(Integer.valueOf(0x1030000 + j)) && Item.itemsList[j] != null && Item.itemsList[j].isDamageable())
            {
                String s2 = StringTranslate.getInstance().translateKeyFormat("stat.breakItem", new Object[]
                        {
                            Item.itemsList[j].getStatName()
                        });
                StatList.objectBreakStats[j] = (new StatCrafting(0x1030000 + j, s2, j)).registerStat();
            }
        }

        HashSet hashset = new HashSet();
        Object obj;

        for (Iterator iterator = CraftingManager.getInstance().getRecipeList().iterator(); iterator.hasNext(); hashset.add(Integer.valueOf(((IRecipe)obj).getRecipeOutput().itemID)))
        {
            obj = iterator.next();
        }

        Object obj1;

        for (Iterator iterator1 = FurnaceRecipes.smelting().getSmeltingList().values().iterator(); iterator1.hasNext(); hashset.add(Integer.valueOf(((ItemStack)obj1).itemID)))
        {
            obj1 = iterator1.next();
        }

        Iterator iterator2 = hashset.iterator();

        do
        {
            if (!iterator2.hasNext())
            {
                break;
            }

            int k = ((Integer)iterator2.next()).intValue();

            if (!StatList.oneShotStats.containsKey(Integer.valueOf(0x1010000 + k)) && Item.itemsList[k] != null)
            {
                String s3 = StringTranslate.getInstance().translateKeyFormat("stat.craftItem", new Object[]
                        {
                            Item.itemsList[k].getStatName()
                        });
                StatList.objectCraftStats[k] = (new StatCrafting(0x1010000 + k, s3, k)).registerStat();
            }
        }
        while (true);
    }

    public static boolean isModLoaded(String s)
    {
        label0:
        {
            Class class1 = null;

            try
            {
                class1 = Class.forName(s);
            }
            catch (ClassNotFoundException classnotfoundexception)
            {
                return false;
            }

            if (class1 == null)
            {
                break label0;
            }

            Iterator iterator = modList.iterator();
            BaseMod basemod;

            do
            {
                if (!iterator.hasNext())
                {
                    break label0;
                }

                basemod = (BaseMod)iterator.next();
            }
            while (!class1.isInstance(basemod));

            return true;
        }
        return false;
    }

    public static void loadConfig() throws IOException
    {
        cfgdir.mkdir();

        if ((cfgfile.exists() || cfgfile.createNewFile()) && cfgfile.canRead())
        {
            FileInputStream fileinputstream = new FileInputStream(cfgfile);
            props.load(fileinputstream);
            fileinputstream.close();
        }
    }

    public static void onItemPickup(EntityPlayer entityplayer, ItemStack itemstack)
    {
        BaseMod basemod;

        for (Iterator iterator = modList.iterator(); iterator.hasNext(); basemod.onItemPickup(entityplayer, itemstack))
        {
            basemod = (BaseMod)iterator.next();
        }
    }

    public static void onTick(MinecraftServer minecraftserver)
    {
        Profiler.endSection();
        Profiler.endSection();
        Profiler.startSection("modtick");

        if (!hasInit)
        {
            init();
            logger.fine("Initialized");
        }

        long l = 0L;

        if (minecraftserver.worldMngr != null && minecraftserver.worldMngr[0] != null)
        {
            l = minecraftserver.worldMngr[0].getWorldTime();
            Iterator iterator = inGameHooks.entrySet().iterator();

            do
            {
                if (!iterator.hasNext())
                {
                    break;
                }

                java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();

                if (clock != l || !((Boolean)entry.getValue()).booleanValue())
                {
                    ((BaseMod)entry.getKey()).onTickInGame(minecraftserver);
                }
            }
            while (true);
        }

        clock = l;
        Profiler.endSection();
        Profiler.startSection("render");
        Profiler.startSection("gameRenderer");
    }

    public static void openGUI(EntityPlayer entityplayer, int i, IInventory iinventory, Container container)
    {
        if (!hasInit)
        {
            init();
            logger.fine("Initialized");
        }

        if (entityplayer instanceof EntityPlayerMP)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)entityplayer;

            try
            {
                method_getNextWindowId.invoke(entityplayermp, new Object[0]);
                int j = field_currentWindowId.getInt(entityplayermp);
                entityplayermp.closeCraftingGui();
                entityplayermp.playerNetServerHandler.sendPacket(new Packet100OpenWindow(j, i, iinventory.getInvName(), iinventory.getSizeInventory()));
                entityplayermp.craftingInventory = container;
                entityplayermp.craftingInventory.windowId = j;
                entityplayermp.craftingInventory.onCraftGuiOpened(entityplayermp);
            }
            catch (InvocationTargetException invocationtargetexception)
            {
                getLogger().throwing("ModLoaderMultiplayer", "OpenModGUI", invocationtargetexception);
                throwException("ModLoaderMultiplayer", invocationtargetexception);
            }
            catch (IllegalAccessException illegalaccessexception)
            {
                getLogger().throwing("ModLoaderMultiplayer", "OpenModGUI", illegalaccessexception);
                throwException("ModLoaderMultiplayer", illegalaccessexception);
            }
        }
    }

    public static void populateChunk(IChunkProvider ichunkprovider, int i, int j, World world)
    {
        if (!hasInit)
        {
            init();
            logger.fine("Initialized");
        }

        Random random = new Random(world.getSeed());
        long l = (random.nextLong() / 2L) * 2L + 1L;
        long l1 = (random.nextLong() / 2L) * 2L + 1L;
        random.setSeed((long)i * l + (long)j * l1 ^ world.getSeed());
        Iterator iterator = modList.iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            BaseMod basemod = (BaseMod)iterator.next();

            if (ichunkprovider instanceof ChunkProviderGenerate)
            {
                basemod.generateSurface(world, random, i << 4, j << 4);
            }
            else if (ichunkprovider instanceof ChunkProviderHell)
            {
                basemod.generateNether(world, random, i << 4, j << 4);
            }
        }
        while (true);
    }

    private static void readFromClassPath(File file) throws FileNotFoundException, IOException
    {
        logger.finer((new StringBuilder()).append("Adding mods from ").append(file.getCanonicalPath()).toString());
        ClassLoader classloader = (net.minecraft.src.ModLoader.class).getClassLoader();

        if (file.isFile() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")))
        {
            logger.finer("Zip found.");
            FileInputStream fileinputstream = new FileInputStream(file);
            ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);

            do
            {
                ZipEntry zipentry = zipinputstream.getNextEntry();

                if (zipentry == null)
                {
                    fileinputstream.close();
                    break;
                }

                String s = zipentry.getName();

                if (!zipentry.isDirectory() && s.startsWith("mod_") && s.endsWith(".class"))
                {
                    addMod(classloader, s);
                }
            }
            while (true);
        }
        else if (file.isDirectory())
        {
            Package package1 = (net.minecraft.src.ModLoader.class).getPackage();

            if (package1 != null)
            {
                String s2 = package1.getName().replace('.', File.separatorChar);
                file = new File(file, s2);
            }

            logger.finer("Directory found.");
            File afile[] = file.listFiles();

            if (afile != null)
            {
                for (int i = 0; i < afile.length; i++)
                {
                    String s1 = afile[i].getName();

                    if (afile[i].isFile() && s1.startsWith("mod_") && s1.endsWith(".class"))
                    {
                        addMod(classloader, s1);
                    }
                }
            }
        }
    }

    private static void readFromModFolder(File file) throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
    {
        ClassLoader classloader = (net.minecraft.server.MinecraftServer.class).getClassLoader();
        Method method = (java.net.URLClassLoader.class).getDeclaredMethod("addURL", new Class[]
                {
                    java.net.URL.class
                });
        method.setAccessible(true);

        if (!file.isDirectory())
        {
            throw new IllegalArgumentException("folder must be a Directory.");
        }

        File afile[] = file.listFiles();
        Arrays.sort(afile);

        if (classloader instanceof URLClassLoader)
        {
            for (int i = 0; i < afile.length; i++)
            {
                File file1 = afile[i];

                if (file1.isDirectory() || file1.isFile() && (file1.getName().endsWith(".jar") || file1.getName().endsWith(".zip")))
                {
                    method.invoke(classloader, new Object[]
                            {
                                file1.toURI().toURL()
                            });
                }
            }
        }

        label0:

        for (int j = 0; j < afile.length; j++)
        {
            File file2 = afile[j];

            if (!file2.isDirectory() && (!file2.isFile() || !file2.getName().endsWith(".jar") && !file2.getName().endsWith(".zip")))
            {
                continue;
            }

            logger.finer((new StringBuilder()).append("Adding mods from ").append(file2.getCanonicalPath()).toString());

            if (file2.isFile())
            {
                logger.finer("Zip found.");
                FileInputStream fileinputstream = new FileInputStream(file2);
                ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);
                Object obj = null;

                do
                {
                    ZipEntry zipentry;
                    String s2;

                    do
                    {
                        zipentry = zipinputstream.getNextEntry();

                        if (zipentry == null)
                        {
                            zipinputstream.close();
                            fileinputstream.close();
                            continue label0;
                        }

                        s2 = zipentry.getName();
                    }
                    while (zipentry.isDirectory() || !s2.startsWith("mod_") || !s2.endsWith(".class"));

                    addMod(classloader, s2);
                }
                while (true);
            }

            if (!file2.isDirectory())
            {
                continue;
            }

            Package package1 = (net.minecraft.src.ModLoader.class).getPackage();

            if (package1 != null)
            {
                String s = package1.getName().replace('.', File.separatorChar);
                file2 = new File(file2, s);
            }

            logger.finer("Directory found.");
            File afile1[] = file2.listFiles();

            if (afile1 == null)
            {
                continue;
            }

            for (int k = 0; k < afile1.length; k++)
            {
                String s1 = afile1[k].getName();

                if (afile1[k].isFile() && s1.startsWith("mod_") && s1.endsWith(".class"))
                {
                    addMod(classloader, s1);
                }
            }
        }
    }

    public static void registerBlock(Block block)
    {
        registerBlock(block, (Class)null);
    }

    public static void registerBlock(Block block, Class class1)
    {
        try
        {
            if (block == null)
            {
                throw new IllegalArgumentException("block parameter cannot be null.");
            }

            int i = block.blockID;
            ItemBlock itemblock = null;

            if (class1 != null)
            {
                itemblock = (ItemBlock)class1.getConstructor(new Class[]
                        {
                            Integer.TYPE
                        }).newInstance(new Object[]
                                {
                                    Integer.valueOf(i - 256)
                                });
            }
            else
            {
                itemblock = new ItemBlock(i - 256);
            }

            if (Block.blocksList[i] != null && Item.itemsList[i] == null)
            {
                Item.itemsList[i] = itemblock;
            }
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", illegalargumentexception);
            throwException(illegalargumentexception);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", illegalaccessexception);
            throwException(illegalaccessexception);
        }
        catch (SecurityException securityexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", securityexception);
            throwException(securityexception);
        }
        catch (InstantiationException instantiationexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", instantiationexception);
            throwException(instantiationexception);
        }
        catch (InvocationTargetException invocationtargetexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", invocationtargetexception);
            throwException(invocationtargetexception);
        }
        catch (NoSuchMethodException nosuchmethodexception)
        {
            logger.throwing("ModLoader", "RegisterBlock", nosuchmethodexception);
            throwException(nosuchmethodexception);
        }
    }

    public static void registerEntityID(Class class1, String s, int i)
    {
        try
        {
            method_RegisterEntityID.invoke((Object)null, new Object[]
                    {
                        class1, s, Integer.valueOf(i)
                    });
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            logger.throwing("ModLoader", "RegisterEntityID", illegalargumentexception);
            throwException(illegalargumentexception);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "RegisterEntityID", illegalaccessexception);
            throwException(illegalaccessexception);
        }
        catch (InvocationTargetException invocationtargetexception)
        {
            logger.throwing("ModLoader", "RegisterEntityID", invocationtargetexception);
            throwException(invocationtargetexception);
        }
    }

    public static void registerTileEntity(Class class1, String s)
    {
        try
        {
            method_RegisterTileEntity.invoke((Object)null, new Object[]
                    {
                        class1, s
                    });
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            logger.throwing("ModLoader", "RegisterTileEntity", illegalargumentexception);
            throwException(illegalargumentexception);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "RegisterTileEntity", illegalaccessexception);
            throwException(illegalaccessexception);
        }
        catch (InvocationTargetException invocationtargetexception)
        {
            logger.throwing("ModLoader", "RegisterTileEntity", invocationtargetexception);
            throwException(invocationtargetexception);
        }
    }

    public static void removeSpawn(Class class1, EnumCreatureType enumcreaturetype)
    {
        removeSpawn(class1, enumcreaturetype, (BiomeGenBase[])null);
    }

    public static void removeSpawn(Class class1, EnumCreatureType enumcreaturetype, BiomeGenBase abiomegenbase[])
    {
        if (class1 == null)
        {
            throw new IllegalArgumentException("entityClass cannot be null");
        }

        if (enumcreaturetype == null)
        {
            throw new IllegalArgumentException("spawnList cannot be null");
        }

        if (abiomegenbase == null)
        {
            abiomegenbase = standardBiomes;
        }

        label0:

        for (int i = 0; i < abiomegenbase.length; i++)
        {
            List list = abiomegenbase[i].getSpawnableList(enumcreaturetype);

            if (list == null)
            {
                continue;
            }

            Iterator iterator = list.iterator();

            do
            {
                SpawnListEntry spawnlistentry;

                do
                {
                    if (!iterator.hasNext())
                    {
                        continue label0;
                    }

                    spawnlistentry = (SpawnListEntry)iterator.next();
                }
                while (spawnlistentry.entityClass != class1);

                iterator.remove();
            }
            while (true);
        }
    }

    public static void removeSpawn(String s, EnumCreatureType enumcreaturetype)
    {
        removeSpawn(s, enumcreaturetype, (BiomeGenBase[])null);
    }

    public static void removeSpawn(String s, EnumCreatureType enumcreaturetype, BiomeGenBase abiomegenbase[])
    {
        Class class1 = (Class)classMap.get(s);

        if (class1 != null && (net.minecraft.src.EntityLiving.class).isAssignableFrom(class1))
        {
            removeSpawn(class1, enumcreaturetype, abiomegenbase);
        }
    }

    public static void saveConfig() throws IOException
    {
        cfgdir.mkdir();

        if ((cfgfile.exists() || cfgfile.createNewFile()) && cfgfile.canWrite())
        {
            FileOutputStream fileoutputstream = new FileOutputStream(cfgfile);
            props.store(fileoutputstream, "ModLoader Config");
            fileoutputstream.close();
        }
    }

    public static void setInGameHook(BaseMod basemod, boolean flag, boolean flag1)
    {
        if (flag)
        {
            inGameHooks.put(basemod, Boolean.valueOf(flag1));
        }
        else
        {
            inGameHooks.remove(basemod);
        }
    }

    public static void setPrivateValue(Class class1, Object obj, int i, Object obj1) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field field = class1.getDeclaredFields()[i];
            field.setAccessible(true);
            int j = field_modifiers.getInt(field);

            if ((j & 0x10) != 0)
            {
                field_modifiers.setInt(field, j & 0xffffffef);
            }

            field.set(obj, obj1);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "setPrivateValue", illegalaccessexception);
            throwException("An impossible error has occured!", illegalaccessexception);
        }
    }

    public static void setPrivateValue(Class class1, Object obj, String s, Object obj1) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field field = class1.getDeclaredField(s);
            int i = field_modifiers.getInt(field);

            if ((i & 0x10) != 0)
            {
                field_modifiers.setInt(field, i & 0xffffffef);
            }

            field.setAccessible(true);
            field.set(obj, obj1);
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            logger.throwing("ModLoader", "setPrivateValue", illegalaccessexception);
            throwException("An impossible error has occured!", illegalaccessexception);
        }
    }

    private static void setupProperties(Class class1) throws IllegalArgumentException, IllegalAccessException, IOException, SecurityException, NoSuchFieldException, NoSuchAlgorithmException, DigestException
    {
        LinkedList linkedlist = new LinkedList();
        Properties properties = new Properties();
        int i = 0;
        int j = 0;
        File file = new File(cfgdir, (new StringBuilder()).append(class1.getSimpleName()).append(".cfg").toString());

        if (file.exists() && file.canRead())
        {
            properties.load(new FileInputStream(file));
        }

        if (properties.containsKey("checksum"))
        {
            j = Integer.parseInt(properties.getProperty("checksum"), 36);
        }

        Field afield[];
        int k = (afield = class1.getDeclaredFields()).length;

        for (int l = 0; l < k; l++)
        {
            Field field = afield[l];

            if ((field.getModifiers() & 8) != 0 && field.isAnnotationPresent(net.minecraft.src.MLProp.class))
            {
                linkedlist.add(field);
                Object obj = field.get((Object)null);
                i += obj.hashCode();
            }
        }

        StringBuilder stringbuilder = new StringBuilder();
        Iterator iterator = linkedlist.iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            Field field1 = (Field)iterator.next();

            if ((field1.getModifiers() & 8) == 0 || !field1.isAnnotationPresent(net.minecraft.src.MLProp.class))
            {
                continue;
            }

            Class class2 = field1.getType();
            MLProp mlprop = (MLProp)field1.getAnnotation(net.minecraft.src.MLProp.class);
            String s = mlprop.name().length() == 0 ? field1.getName() : mlprop.name();
            Object obj1 = field1.get((Object)null);
            StringBuilder stringbuilder1 = new StringBuilder();

            if (mlprop.min() != (-1.0D / 0.0D))
            {
                stringbuilder1.append(String.format(",>=%.1f", new Object[]
                        {
                            Double.valueOf(mlprop.min())
                        }));
            }

            if (mlprop.max() != (1.0D / 0.0D))
            {
                stringbuilder1.append(String.format(",<=%.1f", new Object[]
                        {
                            Double.valueOf(mlprop.max())
                        }));
            }

            StringBuilder stringbuilder2 = new StringBuilder();

            if (mlprop.info().length() > 0)
            {
                stringbuilder2.append(" -- ");
                stringbuilder2.append(mlprop.info());
            }

            stringbuilder.append(String.format("%s (%s:%s%s)%s\n", new Object[]
                    {
                        s, class2.getName(), obj1, stringbuilder1, stringbuilder2
                    }));

            if (j == i && properties.containsKey(s))
            {
                String s1 = properties.getProperty(s);
                Object obj2 = null;

                if (class2.isAssignableFrom(java.lang.String.class))
                {
                    obj2 = s1;
                }
                else if (class2.isAssignableFrom(Integer.TYPE))
                {
                    obj2 = Integer.valueOf(Integer.parseInt(s1));
                }
                else if (class2.isAssignableFrom(Short.TYPE))
                {
                    obj2 = Short.valueOf(Short.parseShort(s1));
                }
                else if (class2.isAssignableFrom(Byte.TYPE))
                {
                    obj2 = Byte.valueOf(Byte.parseByte(s1));
                }
                else if (class2.isAssignableFrom(Boolean.TYPE))
                {
                    obj2 = Boolean.valueOf(Boolean.parseBoolean(s1));
                }
                else if (class2.isAssignableFrom(Float.TYPE))
                {
                    obj2 = Float.valueOf(Float.parseFloat(s1));
                }
                else if (class2.isAssignableFrom(Double.TYPE))
                {
                    obj2 = Double.valueOf(Double.parseDouble(s1));
                }

                if (obj2 == null)
                {
                    continue;
                }

                if (obj2 instanceof Number)
                {
                    double d = ((Number)obj2).doubleValue();

                    if (mlprop.min() != (-1.0D / 0.0D) && d < mlprop.min() || mlprop.max() != (1.0D / 0.0D) && d > mlprop.max())
                    {
                        continue;
                    }
                }

                logger.finer((new StringBuilder()).append(s).append(" set to ").append(obj2).toString());

                if (!obj2.equals(obj1))
                {
                    field1.set((Object)null, obj2);
                }
            }
            else
            {
                logger.finer((new StringBuilder()).append(s).append(" not in config, using default: ").append(obj1).toString());
                properties.setProperty(s, obj1.toString());
            }
        }
        while (true);

        properties.put("checksum", Integer.toString(i, 36));

        if (!properties.isEmpty() && (file.exists() || file.createNewFile()) && file.canWrite())
        {
            properties.store(new FileOutputStream(file), stringbuilder.toString());
        }
    }

    private static void sortModList() throws Exception
    {
        HashMap hashmap = new HashMap();
        BaseMod basemod;

        for (Iterator iterator = getLoadedMods().iterator(); iterator.hasNext(); hashmap.put(basemod.getClass().getSimpleName(), basemod))
        {
            basemod = (BaseMod)iterator.next();
        }

        LinkedList linkedlist = new LinkedList();
        label0:

        for (int i = 0; linkedlist.size() != modList.size() && i <= 10; i++)
        {
            Iterator iterator1 = modList.iterator();
            label1:

            do
            {
                if (!iterator1.hasNext())
                {
                    continue label0;
                }

                BaseMod basemod1 = (BaseMod)iterator1.next();

                if (linkedlist.contains(basemod1))
                {
                    continue;
                }

                String s = basemod1.getPriorities();

                if (s != null && s.length() != 0 && s.indexOf(':') != -1)
                {
                    if (i <= 0)
                    {
                        continue;
                    }

                    int j = -1;
                    int k = 0x80000000;
                    int l = 0x7fffffff;
                    String as[];

                    if (s.indexOf(';') > 0)
                    {
                        as = s.split(";");
                    }
                    else
                    {
                        as = (new String[]
                                {
                                    s
                                });
                    }

                    for (int i1 = 0; i1 < as.length; i1++)
                    {
                        String s1 = as[i1];

                        if (s1.indexOf(':') == -1)
                        {
                            continue;
                        }

                        String as1[] = s1.split(":");
                        String s2 = as1[0];
                        String s3 = as1[1];

                        if (!s2.contentEquals("required-before") && !s2.contentEquals("before") && !s2.contentEquals("after") && !s2.contentEquals("required-after"))
                        {
                            continue;
                        }

                        if (s3.contentEquals("*"))
                        {
                            if (!s2.contentEquals("required-before") && !s2.contentEquals("before"))
                            {
                                if (s2.contentEquals("required-after") || s2.contentEquals("after"))
                                {
                                    j = linkedlist.size();
                                }
                            }
                            else
                            {
                                j = 0;
                            }

                            break;
                        }

                        if ((s2.contentEquals("required-before") || s2.contentEquals("required-after")) && !hashmap.containsKey(s3))
                        {
                            throw new Exception(String.format("%s is missing dependency: %s", new Object[]
                                    {
                                        basemod1, s3
                                    }));
                        }

                        BaseMod basemod2 = (BaseMod)hashmap.get(s3);

                        if (!linkedlist.contains(basemod2))
                        {
                            continue label1;
                        }

                        int j1 = linkedlist.indexOf(basemod2);

                        if (!s2.contentEquals("required-before") && !s2.contentEquals("before"))
                        {
                            if (!s2.contentEquals("required-after") && !s2.contentEquals("after"))
                            {
                                continue;
                            }

                            j = j1 + 1;

                            if (j > k)
                            {
                                k = j;
                            }
                            else
                            {
                                j = k;
                            }

                            continue;
                        }

                        j = j1;

                        if (j1 < l)
                        {
                            l = j1;
                        }
                        else
                        {
                            j = l;
                        }
                    }

                    if (j != -1)
                    {
                        linkedlist.add(j, basemod1);
                    }
                }
                else
                {
                    linkedlist.add(basemod1);
                }
            }
            while (true);
        }

        modList.clear();
        modList.addAll(linkedlist);
    }

    public static void takenFromCrafting(EntityPlayer entityplayer, ItemStack itemstack, IInventory iinventory)
    {
        BaseMod basemod;

        for (Iterator iterator = modList.iterator(); iterator.hasNext(); basemod.takenFromCrafting(entityplayer, itemstack, iinventory))
        {
            basemod = (BaseMod)iterator.next();
        }
    }

    public static void takenFromFurnace(EntityPlayer entityplayer, ItemStack itemstack)
    {
        BaseMod basemod;

        for (Iterator iterator = modList.iterator(); iterator.hasNext(); basemod.takenFromFurnace(entityplayer, itemstack))
        {
            basemod = (BaseMod)iterator.next();
        }
    }

    public static void throwException(String s, Throwable throwable)
    {
        throwable.printStackTrace();
        logger.log(Level.SEVERE, "Unexpected exception", throwable);
        MinecraftServer.logger.throwing("ModLoader", s, throwable);
        throw new RuntimeException(s, throwable);
    }

    private static void throwException(Throwable throwable)
    {
        throwException("Exception occured in ModLoader", throwable);
    }

    public static void initialize(MinecraftServer minecraftserver)
    {
        instance = minecraftserver;

        try
        {
            String s = (net.minecraft.src.ModLoader.class).getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            s = s.substring(0, s.lastIndexOf('/'));
            cfgdir = new File(s, "/config/");
            cfgfile = new File(s, "/config/ModLoader.cfg");
            logfile = new File(s, "ModLoader.txt");
            modDir = new File(s, "/mods/");
        }
        catch (URISyntaxException urisyntaxexception)
        {
            getLogger().throwing("ModLoader", "Init", urisyntaxexception);
            throwException("ModLoader", urisyntaxexception);
            return;
        }

        try
        {
            try
            {
                method_getNextWindowId = (net.minecraft.src.EntityPlayerMP.class).getDeclaredMethod("bc", (Class[])null);
            }
            catch (NoSuchMethodException nosuchmethodexception)
            {
                method_getNextWindowId = (net.minecraft.src.EntityPlayerMP.class).getDeclaredMethod("getNextWidowId", (Class[])null);
            }

            method_getNextWindowId.setAccessible(true);

            try
            {
                field_currentWindowId = (net.minecraft.src.EntityPlayerMP.class).getDeclaredField("cl");
            }
            catch (NoSuchFieldException nosuchfieldexception)
            {
                field_currentWindowId = (net.minecraft.src.EntityPlayerMP.class).getDeclaredField("currentWindowId");
            }

            field_currentWindowId.setAccessible(true);
        }
        catch (NoSuchFieldException nosuchfieldexception1)
        {
            getLogger().throwing("ModLoader", "Init", nosuchfieldexception1);
            throwException("ModLoader", nosuchfieldexception1);
            return;
        }
        catch (NoSuchMethodException nosuchmethodexception1)
        {
            getLogger().throwing("ModLoader", "Init", nosuchmethodexception1);
            throwException("ModLoader", nosuchmethodexception1);
            return;
        }

        init();
    }

    static
    {
        cfgLoggingLevel = Level.FINER;
    }
}
