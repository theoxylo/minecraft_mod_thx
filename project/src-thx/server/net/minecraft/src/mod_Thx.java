package net.minecraft.src;

public class mod_Thx extends BaseModMp
{
    public static mod_Thx instance;

    public mod_Thx()
    {
        log("mod_Thx() called");

        instance = this; // for easy access by static methods and to instance methods
    }

    @Override
    public void load()
    {
        log("load() called");

        ThxConfig.loadProperties();

        ModLoader.setInGameHook(this, true, true);

        log("enabling flight on server");
        ModLoader.getMinecraftServerInstance().allowFlight = true;
        log("server.allowFlight: " + ModLoader.getMinecraftServerInstance().allowFlight);

        int distance = 200; // 160; // spawn/despawn at this distance from entity
        int frequency = 1; // ticks per update, 1 to 60 (20 ticks/sec)
        
        // register entity classes
        helicopter:
        {
            ModLoaderMp.registerEntityTrackerEntry(ThxEntityHelicopter.class, 75);
            ModLoaderMp.registerEntityTracker(ThxEntityHelicopter.class, distance, frequency);

            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Helicopter with entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityHelicopter.class, "thxHelicopter", entityId);
        }
        rocket:
        {
            boolean hasOwner = true;
            ModLoaderMp.registerEntityTrackerEntry(ThxEntityRocket.class, hasOwner, 76);
            ModLoaderMp.registerEntityTracker(ThxEntityRocket.class, distance, frequency);
            
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Rocket with entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityMissile.class, "thxRocket", entityId);
        }
        missile:
        {
            ModLoaderMp.registerEntityTrackerEntry(ThxEntityMissile.class, 77);
            ModLoaderMp.registerEntityTracker(ThxEntityMissile.class, distance, frequency);
            
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Missile with entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityMissile.class, "thxMissile", entityId);
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
            ModLoader.addRecipe(itemStack, recipe);
        }

        log("Done loading " + getVersion());
    }

    @Override
    public String getVersion()
    {
        // log("getVersion called");
        return "Minecraft THX Helicopter Mod - mod_thx-mc123_v016_bp1";
    }

    @Override
    public void handlePacket(Packet230ModLoader packet, EntityPlayerMP player)
    {
        // log("Received packet type " + packet.packetType + " from player " + player);
        // log("player.ridingEntity: " + player.ridingEntity); // TODO: coming up null

        if (player.ridingEntity instanceof IClientDriven)
        {
            // log("Packet is for helicopter update");
            ((ThxEntity) player.ridingEntity).latestUpdatePacket = packet;
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
        if (ThxConfig.ENABLE_LOGGING) System.out.println("mod_thx_server: " + s);
    }
}
