package net.minecraft.src;

public class mod_Thx extends BaseModMp
{
    public static mod_Thx instance;

    public mod_Thx()
    {
        log("mod_Thx() called");

        /*
         * ModLoaderMp.RegisterNetClientHandlerEntity(classMissile, 73); 
         * ModLoaderMp.RegisterNetClientHandlerEntity(classRocket, 74);
         */
        instance = this; // for easy access by static methods and to instance methods
    }

    @Override
    public void load()
    {
        log("load() called");

        ThxConfig.loadProperties();

        ModLoader.SetInGameHook(this, true, true);

        // register entity classes
        helicopter:
        {
            ModLoaderMp.RegisterNetClientHandlerEntity(ThxEntityHelicopter.class, 75);
            
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Helicopter with entity id " + entityId);
            ModLoader.RegisterEntityID(ThxEntityHelicopter.class, "thxHelicopter", entityId);
        }
        rocket:
        {
            ModLoaderMp.RegisterNetClientHandlerEntity(ThxEntityRocket.class, 76);
            
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Rocket with entity id " + entityId);
            ModLoader.RegisterEntityID(ThxEntityRocket.class, "thxRocket", entityId);
        }
        missile:
        {
            ModLoaderMp.RegisterNetClientHandlerEntity(ThxEntityMissile.class, 77);
            
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Missile with entity id " + entityId);
            ModLoader.RegisterEntityID(ThxEntityMissile.class, "thxMissile", entityId);
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
            ModLoader.AddName(item, "THX Helicopter Prototype");

            log("Adding recipe for helicopter");
            ItemStack itemStack = new ItemStack(item, 1, 1);
            Object[] recipe = new Object[] { " X ", "X X", "XXX", Character.valueOf('X'), Block.planks };
            ModLoader.AddRecipe(itemStack, recipe);
        }

        log("Done loading " + getVersion());
    }

    @Override
    public void AddRenderer(java.util.Map map)
    {
        map.put(ThxEntityHelicopter.class, new ThxRender());
        map.put(ThxEntityRocket.class, new ThxRender());
        map.put(ThxEntityMissile.class, new ThxRender());
    }

    @Override
    public String getVersion()
    {
        //log("getVersion called");
        return "Minecraft THX Helicopter Mod - mod_thx-mc110_v016";
    }

    @Override
    public void HandlePacket(Packet230ModLoader packet)
    {
        //log("Received packet type " + packet.packetType + ": " + packet);
        
        int entityId = packet.dataInt[0];
        Entity entity = ((WorldClient) ModLoader.getMinecraftInstance().theWorld).getEntityByID(entityId);
        ((ThxEntity) entity).handleUpdatePacketFromServer(packet);
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
        System.out.println("mod_thx_client: " + s);
    }
}
