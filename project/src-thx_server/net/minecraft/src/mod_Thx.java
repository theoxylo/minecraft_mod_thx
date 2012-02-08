package net.minecraft.src;

public class mod_Thx extends BaseModMp
{
    public static mod_Thx instance;

    /*
     * coming soon! static Class classMissile = net.minecraft.src.ThxEntityMissile.class; static Class classRocket = net.minecraft.src.ThxEntityRocket.class;
     */

    public mod_Thx()
    {
        log("mod_Thx() called");

        /*
         * coming soon! ModLoaderMp.RegisterEntityTrackerEntry(classMissile, 73); ModLoaderMp.RegisterEntityTracker(classMissile, 160, 5);
         * 
         * ModLoaderMp.RegisterEntityTrackerEntry(classRocket, 74); ModLoaderMp.RegisterEntityTracker(classRocket, 160, 5);
         */
        instance = this; // for easy access by static methods and to instance methods
    }

    @Override
    public void load()
    {
        log("load() called");
        
        log("server.allowFlight: " + ModLoader.getMinecraftServerInstance().allowFlight);

        ThxConfig.loadProperties();

        ModLoader.SetInGameHook(this, true, true);

        // register entity classes
        /*
         * coming soon! missile: { int entityId = ModLoader.getUniqueEntityId(); log("Registering entity class for Missile with entity id " + entityId); ModLoader.RegisterEntityID(classMissile, "thxMissile", entityId); }
         */
        helicopter:
        {
        log("enabling flight on server");
        ModLoader.getMinecraftServerInstance().allowFlight = true;

        log("registering entity trackers and tracker entries");
        int netId = 75;
        ModLoaderMp.RegisterEntityTrackerEntry(ThxEntityHelicopter.class, netId);
        
        int distance = 30; //160;
        int frequency = 20;
        ModLoaderMp.RegisterEntityTracker(ThxEntityHelicopter.class, distance, frequency);

            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Helicopter with entity id " + entityId);
            ModLoader.RegisterEntityID(ThxEntityHelicopter.class, "thxHelicopter", entityId);
        }

        helicopterItem:
        {
            int itemId = getNextItemId();
            log("Setting up inventory item for helicopter with item id " + itemId);
            Item item = new ThxItemHelicopter(itemId);

            if (ThxConfig.getBoolProperty("disable_helicopter_item_image"))
            {
                item.setIconIndex(92); // hard-code to cookie icon for compatibility
            }
            else
            {
                item.setIconIndex(ModLoader.addOverride("/gui/items.png", "/thx/helicopter_icon.png"));
            }
            item.setItemName("thxHelicopter");
            // ModLoader.AddName(item, "THX Helicopter Prototype");

            log("Adding recipe for helicopter");
            ItemStack itemStack = new ItemStack(item, 1, 1);
            Object[] recipe = new Object[] { " X ", "X X", "XXX", Character.valueOf('X'), Block.planks };
            ModLoader.AddRecipe(itemStack, recipe);
        }

        log("Done loading " + getVersion());
    }

    @Override
    public String getVersion()
    {
        log("getVersion called");
        return "Minecraft THX Helicopter Mod - mod_thx-mc110_v016";
    }

    @Override
    public void HandlePacket(Packet230ModLoader packet, EntityPlayerMP player)
    {
        log("Received packet type " + packet.packetType + " from player " + player);
        
        log("player.ridingEntity: " + player.ridingEntity); // TODO: coming up null
        
        if (player.ridingEntity instanceof ThxEntityHelicopter)
        {
            log("Packet is for helicopter update");
            
            ((ThxEntity)player.ridingEntity).handleUpdatePacket(packet);
        }
    }

    int getNextItemId()
    {
        // return next available id
        for (int idx = 24000; idx + 256 < Item.itemsList.length; idx++)
        {
            if (Item.itemsList[idx + 256] == null) return idx;
        }
        // error:
        throw new RuntimeException("Could not find next available Item ID -- can't continue!");
    }

    public static void log(String s)
    {
        System.out.println("mod_thx_server: " + s);
    }
}
