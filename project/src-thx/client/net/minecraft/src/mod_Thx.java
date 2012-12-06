package net.minecraft.src;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;

public class mod_Thx extends BaseMod
{
    static ThxConfig config;
    
    public static mod_Thx instance;

    static int HELICOPTER_TYPE_ID = 99;
    
    public mod_Thx()
    {
        System.out.println("mod_Thx() called");
		config = new ThxConfig();
        instance = this; // for easy access by static methods and to instance methods
    }

    @Override
    public void load()
    {
        log("load() called");

        ModLoader.setInGameHook(this, true, true);
        ModLoader.registerPacketChannel(this, "THX_entity");

        // register entity classes
        helicopter:
        {
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Helicopter with entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityHelicopter.class, "thxHelicopter", entityId);
            
            int drawDistance = 8; // typically 160
            int updateFreq = 1;
            boolean trackMotion = true;
            ModLoader.addEntityTracker(this, ThxEntityHelicopter.class, HELICOPTER_TYPE_ID, drawDistance, updateFreq, trackMotion);
        }
        rocket:
        {
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Rocket with entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityRocket.class, "thxRocket", entityId);
        }
        missile:
        {
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Missile with entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityMissile.class, "thxMissile", entityId);
        }

        helicopterItem:
        {
            int itemId = getNextItemId();
            log("Setting up inventory item for helicopter with item id " + itemId);
            Item item = new ThxItemHelicopter(itemId);

            if (config.getBoolProperty("disable_helicopter_item_image"))
            {
                item.setIconIndex(92); // hard-code to cookie icon for compatibility
            }
            else
            {
                item.setIconIndex(ModLoader.addOverride("/gui/items.png", "/thx/helicopter_icon.png"));
            }
            item.setItemName("thxHelicopter");
            ModLoader.addName(item, "THX Helicopter Prototype");

            log("Adding recipe for helicopter");
            ItemStack itemStack = new ItemStack(item, 1, 1);
            Object[] recipe = new Object[] { " X ", "X X", "XXX", Character.valueOf('X'), Block.planks };
            ModLoader.addRecipe(itemStack, recipe);
        }

        log("Done loading " + getVersion());
    }

    @Override
    public void addRenderer(java.util.Map map)
    {
        map.put(ThxEntityHelicopter.class, new ThxRender());
        map.put(ThxEntityRocket.class, new ThxRender());
        map.put(ThxEntityMissile.class, new ThxRender());
    }

    @Override
    public String getVersion()
    {
        //log("getVersion called");
        return "Minecraft THX Helicopter Mod - mod_thx-mc145_v020";
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
        if (ThxConfig.ENABLE_LOGGING) System.out.println("mod_thx: " + s);
    }
    
    static String getProperty(String name)
    {
        return config.getProperty(name);
    }
    
    static int getIntProperty(String name)
    {
        return config.getIntProperty(name);
    }
    
    static boolean getBoolProperty(String name)
    {
        return config.getBoolProperty(name);
    }
    
    static EntityPlayer getEntityPlayerById(int id)
    {
        Object[] players = ModLoader.getMinecraftInstance().theWorld.playerEntities.toArray();

        for (int i = 0; i < players.length; i++)
        {
            EntityPlayer ep = (EntityPlayer) players[i];
            if (ep != null && ep.entityId == id) return ep;
        }
        return null;
    }
    
    @Override
    public Entity spawnEntity(int type, World world, double posX, double posY, double posZ)
    {
        if (type == HELICOPTER_TYPE_ID)
        {
            return new ThxEntityHelicopter(world, posX, posY, posZ, 0f);
        }
        
        return null;
    }
    
    @Override
    public Packet23VehicleSpawn getSpawnPacket(Entity entity, int type)
    {
        log("Creating spawn packet for entity: " + entity);
        return new Packet23VehicleSpawn(entity, type);
    }
    
    @Override
    public void clientCustomPayload(NetClientHandler netHandler, Packet250CustomPayload packet250)
    {
        if (packet250 instanceof ThxEntityPacket250)
        {
            ThxEntityPacket250 packet = (ThxEntityPacket250) packet250;
	        int entityId = packet.dataInt[0];
	        
	        Entity entity = ((WorldClient) ModLoader.getMinecraftInstance().theWorld).getEntityByID(entityId);
	        
            // try calling applyUpdatePacket(packet);
	        //if (entity instanceof ThxEntity) ((ThxEntity) entity).applyUpdatePacket(packet);
	        
	        if (entity instanceof ThxEntity) ((ThxEntity) entity).lastUpdatePacket = packet;
        }
    }

    @Override
    public void serverCustomPayload(NetServerHandler netHandler, Packet250CustomPayload packet250)
    {
        if (packet250 instanceof ThxEntityPacket250)
        {
            ThxEntityPacket250 packet = (ThxEntityPacket250) packet250;
	        if (netHandler.playerEntity.ridingEntity instanceof ThxClientDriven)
	        {
	            // try calling applyUpdatePacket(packet);
	            ((ThxEntity) netHandler.playerEntity.ridingEntity).applyUpdatePacket(packet);
	            //((ThxEntity) player.ridingEntity).latestUpdatePacket = packet;
	        }
        }
    }

}
