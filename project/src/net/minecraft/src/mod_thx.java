package net.minecraft.src;

import java.util.Map;

public class mod_thx extends BaseMod
{

    static int HELICOPTER_TYPE_ID = 99;
    static int ROCKET_TYPE_ID     = 100;
    static int MISSILE_TYPE_ID    = 101;
    
    static WorldClient theWorld = null;
    
    public mod_thx()
    {
        System.out.println("Constructor mod_thx()");
    }

    @Override
    public void load()
    {
        log("Loading...");

        ModLoader.setInGameHook(this, true, true);
        ModLoader.registerPacketChannel(this, "THX_entity");

        int drawDistance = 20; // typically 160, reduced for testing spawn/despawn
        int updateFreq = 2; // 20 for 1 second updates, 2 for every other tick
        boolean trackMotion = true;
            
        // register entity classes
        helicopter:
        {
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Helicopter with ModLoader entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityHelicopter.class, "thxHelicopter", entityId);

            int id = ThxConfig.getIntProperty("thx_id_entity_helicopter"); // can be overridden in prop file only if needed
            if (id > 0) HELICOPTER_TYPE_ID = id;
            log("Adding entity tracker for Helicopter with entity type id " + HELICOPTER_TYPE_ID);
            ModLoader.addEntityTracker(this, ThxEntityHelicopter.class, HELICOPTER_TYPE_ID, drawDistance, updateFreq, trackMotion);
        }
        rocket:
        {
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Rocket with ModLoader entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityRocket.class, "thxRocket", entityId);

            int id = ThxConfig.getIntProperty("thx_id_entity_rocket"); // can be overridden in prop file only if needed
            if (id > 0) ROCKET_TYPE_ID = id;
            log("Adding entity tracker for Rocket with entity type id " + ROCKET_TYPE_ID);
            ModLoader.addEntityTracker(this, ThxEntityRocket.class, ROCKET_TYPE_ID, drawDistance, updateFreq, trackMotion);
        }
        missile:
        {
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Missile with ModLoader entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityMissile.class, "thxMissile", entityId);

            int id = ThxConfig.getIntProperty("thx_id_entity_missile"); // can be overridden in prop file only if needed
            if (id > 0) MISSILE_TYPE_ID = id;
            log("Adding entity tracker for Missile with entity type id " + MISSILE_TYPE_ID);
            ModLoader.addEntityTracker(this, ThxEntityMissile.class, MISSILE_TYPE_ID, drawDistance, updateFreq, trackMotion);
        }

        helicopterItem:
        {
            int itemId = ThxConfig.getIntProperty("thx_id_item_helicopter"); // can be overridden in prop file if needed
            if (itemId == 0) itemId = getNextItemId(); // not defined in prop file
            log("Setting up inventory item for helicopter with item id " + itemId);
            Item item = new ThxItemHelicopter(itemId);

            if (ThxConfig.getBoolProperty("disable_helicopter_item_image"))
            {
                item.setIconIndex(92); // hard-code to cookie icon for compatibility override
            }
            else
            {
                item.setIconIndex(ModLoader.addOverride("/gui/items.png", "/thx/helicopter_icon.png"));
            }

            ModLoader.addName(item, "THX Helicopter Prototype");

            log("Adding recipe for helicopter item: " + item);
            ItemStack itemStack = new ItemStack(item);
            Object[] recipe = new Object[] { " X ", "X X", "XXX", Character.valueOf('X'), Block.planks };
            ModLoader.addRecipe(itemStack, recipe);
        }

        log("Done loading " + getVersion());
    }

    @Override
    public void addRenderer(Map renderers)
    {
        renderers.put(ThxEntityHelicopter.class, new ThxRender());
        renderers.put(ThxEntityRocket.class, new ThxRender());
        renderers.put(ThxEntityMissile.class, new ThxRender());
    }

    @Override
    public String getVersion()
    {
        //log("getVersion called");
        return "Minecraft THX Helicopter Mod - mod_thx-mc146_v020_g";
    }

    int getNextItemId()
    {
        // return next available id
        for (int idx = 2000; idx + 256 < Item.itemsList.length; idx++)
        {
            if (Item.itemsList[idx + 256] == null) return idx;
        }
        // error:
        throw new RuntimeException("Could not autofind next available Item ID -- please set manually in options file and restart");
    }

    public static void log(String s)
    {
        if (!ThxConfig.ENABLE_LOGGING) return;
        
        if (theWorld == null) theWorld = ModLoader.getMinecraftInstance().theWorld;
        
        System.out.println(String.format("[%5d] mod_thx: ", theWorld != null ? theWorld.getWorldTime() : 0)  + s);
    }
    
    public static void plog(String s) // periodic log
    {
        if (!ThxConfig.ENABLE_LOGGING) return;
        if (theWorld == null) theWorld = ModLoader.getMinecraftInstance().theWorld;
        if (theWorld != null && theWorld.getWorldTime() % 60 == 0)
        {
            log(s); //
        }
    }
    
    @Override
    public Entity spawnEntity(int type, World world, double posX, double posY, double posZ)
    {
        if (type == HELICOPTER_TYPE_ID)
        {
            return new ThxEntityHelicopter(world, posX, posY, posZ, 0f);
        }
        
        if (type == ROCKET_TYPE_ID)
        {
            return new ThxEntityRocket(world, posX, posY, posZ);
        }
        
        if (type == MISSILE_TYPE_ID)
        {
            return new ThxEntityMissile(world, posX, posY, posZ);
        }
        
        return null;
    }
    
    @Override
    public Packet23VehicleSpawn getSpawnPacket(Entity entity, int type)
    {
        log("Creating spawn packet for entity: " + entity);
        return new Packet23VehicleSpawn(entity, type, 1); // 1 is the id of the "thrower", it will cause velocity to get updated at spawn
    }
    
    // client received update packet from server
    @Override
    public void clientCustomPayload(NetClientHandler netHandler, Packet250CustomPayload packet)
    {
        EntityClientPlayerMP thePlayer = ModLoader.getMinecraftInstance().thePlayer;
        
        ThxEntityPacket250Data data = new ThxEntityPacket250Data(packet);
        
        plog("Client received packet250: " + data);
        
        ThxEntity entity = (ThxEntity) thePlayer.worldObj.getEntityByID(data.entityId);
        if (entity != null)
        {
            entity.lastUpdatePacket = data;
            
            // call apply directly instead of waiting for next onUpdate?
            //entity.applyUpdatePacketFromServer(data);
        }
        else
        {
            log("ERROR: client received update packet for unknown entity id " + data.entityId);
        }
    }

    // server received update packet from client
    @Override
    public void serverCustomPayload(NetServerHandler netHandler, Packet250CustomPayload packet)
    {
        EntityPlayerMP thePlayer = netHandler.playerEntity;
        
        ThxEntityPacket250Data data = new ThxEntityPacket250Data(packet);
        
        ThxEntity entity = (ThxEntity) thePlayer.worldObj.getEntityByID(data.entityId);
        if (entity != null)
        {
            entity.lastUpdatePacket = data;
            
            // call apply directly instead of waiting for next onUpdate?
            //entity.applyUpdatePacketFromClient(data);
        }
        else
        {
            log("ERROR: server received update packet for unknown entity id " + data.entityId);
	    }
    }
    
    @Override
    public void keyboardEvent(KeyBinding kb) 
    {
        log("keyboardEvent: " + kb);
    }
}
