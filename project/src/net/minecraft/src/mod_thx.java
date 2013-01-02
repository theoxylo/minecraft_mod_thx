package net.minecraft.src;

import java.util.Map;

//@Mod(modid = "mod_thx_forge", name = "Mod THX Forge", version = "0.0.20")
//@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class mod_thx extends BaseMod
{
    //@Instance("mod_thx_forge")
    //public static mod_thx instance;

    //@SidedProxy(clientSide = "thx.ThxProxyClient", serverSide = "thx.ThxProxyServer")
    //public static ThxProxy proxy;

	public static Item thxHelicopterItem;
	
    static WorldClient theWorld = null;
    
    static int HELICOPTER_ENTITY_ID = 0;
    static int ROCKET_ENTITY_ID     = 0;
    static int MISSILE_ENTITY_ID    = 0;
    static int HELICOPTER_ITEM_ID   = 0;

    public mod_thx()
    {
        System.out.println("Constructor mod_thx()");
        
        //instance = this; // for easy access by static methods and to instance methods
    }
    
    @Override
    public void load()
    {
        System.out.println("mod_thx - load() called");
        
	    HELICOPTER_ITEM_ID   = ThxConfig.getIntProperty("thx_item_id_helicopter");
	    //HELICOPTER_ENTITY_ID = ThxConfig.getIntProperty("thx_entity_id_helicopter");
	    //ROCKET_ENTITY_ID     = ThxConfig.getIntProperty("thx_entity_id_rocket");
	    //MISSILE_ENTITY_ID    = ThxConfig.getIntProperty("thx_entity_id_missile");
        
        //log("isClient: " + isClient());
        //log("isServer: " + isServer());
        //log("proxy name: " + proxy.getName());
        //log("proxy class: " + proxy.getClass());
        

        ModLoader.setInGameHook(this, true, true);
        // Currently causing:
        //java.lang.ClassCastException: cpw.mods.fml.common.FMLModContainer cannot be cast to cpw.mods.fml.common.modloader.ModLoaderModContainer
        //at cpw.mods.fml.common.modloader.ModLoaderHelper.updateStandardTicks(ModLoaderHelper.java:67)
        //at net.minecraft.src.ModLoader.setInGameHook(ModLoader.java:850)
        
        
        ModLoader.registerPacketChannel(this, "THX_entity");

        int drawDistance = 64; // typically 160, reduced for testing spawn/despawn
        int updateFreq = 2; // 20 for 1 second updates, 2 for every other tick
        boolean trackMotion = true;
            
        // register entity classes
        helicopter:
        {
            //log("Registering entity class for Helicopter with ModLoader entity id " + HELICOPTER_ENTITY_ID);
            //EntityRegistry.registerModEntity(ThxEntityHelicopter.class, "thxHelicopter", HELICOPTER_ENTITY_ID, instance, drawDistance, updateFreq, trackMotion);
            
            HELICOPTER_ENTITY_ID = ModLoader.getUniqueEntityId();
            log("Registering entity class for Helicopter with ModLoader entity id " + HELICOPTER_ENTITY_ID);
            ModLoader.registerEntityID(ThxEntityHelicopter.class, "thxHelicopter", HELICOPTER_ENTITY_ID);
            ModLoader.addEntityTracker(this, ThxEntityHelicopter.class, HELICOPTER_ENTITY_ID, drawDistance, updateFreq, trackMotion);
        }
        rocket:
        {
            //log("Registering entity class for Rocket with entity id " + ROCKET_ENTITY_ID);
            //EntityRegistry.registerModEntity(ThxEntityRocket.class, "thxRocket", ROCKET_ENTITY_ID, instance, drawDistance, updateFreq, trackMotion);
            
            ROCKET_ENTITY_ID = ModLoader.getUniqueEntityId();
            log("Registering entity class for Rocket with entity id " + ROCKET_ENTITY_ID);
            ModLoader.registerEntityID(ThxEntityRocket.class, "thxRocket", ROCKET_ENTITY_ID);
            ModLoader.addEntityTracker(this, ThxEntityRocket.class, ROCKET_ENTITY_ID, drawDistance, updateFreq, trackMotion);
        }
        missile:
        {
            //log("Registering entity class for Missile with entity id " + MISSILE_ENTITY_ID);
            //EntityRegistry.registerModEntity(ThxEntityMissile.class, "thxMissile", MISSILE_ENTITY_ID, instance, drawDistance, updateFreq, trackMotion);
            
            MISSILE_ENTITY_ID = ModLoader.getUniqueEntityId();
            log("Registering entity class for Missile with entity id " + MISSILE_ENTITY_ID);
            ModLoader.registerEntityID(ThxEntityMissile.class, "thxMissile", MISSILE_ENTITY_ID);
            ModLoader.addEntityTracker(this, ThxEntityMissile.class, MISSILE_ENTITY_ID, drawDistance, updateFreq, trackMotion);
        }

        helicopterItem:
        {
		    if (HELICOPTER_ITEM_ID == 0) HELICOPTER_ITEM_ID = getNextItemId();
            log("Setting up inventory item for helicopter with item id " + HELICOPTER_ITEM_ID);
	        thxHelicopterItem = new ThxItemHelicopter(HELICOPTER_ITEM_ID);
	        /* chained method approach:
	            .setItemName("thxHelicopterItem")
                .setMaxStackSize(16)
		        .setTextureFile("/thx/helicopter_icon.png")
		        .setIconIndex(0)
		        .setMaxDamage(0)
		        .setCreativeTab(CreativeTabs.tabTransport)
	            ;
            */
	        //LanguageRegistry.addName(thxHelicopterItem, "THX Helicopter Prototype");
	        ModLoader.addName(thxHelicopterItem, "THX Helicopter Prototype");

            log("Adding recipe for helicopter");
	        //ItemStack itemStack = new ItemStack(thxHelicopterItem.shiftedIndex, 1, -1);
	        ItemStack itemStack = new ItemStack(thxHelicopterItem);
            Object[] recipe = new Object[] { " X ", "X X", "XXX", Character.valueOf('X'), Block.planks };
            //GameRegistry.addRecipe(itemStack, recipe);
            ModLoader.addRecipe(itemStack, recipe);
        }

        //proxy.initRendering();
        
        log("Done loading " + getVersion());
    }
    
        @Override
    public void addRenderer(Map renderers)
    //public void addRenderer(Map<Class<? extends Entity>, Render> renderers)
    {
        renderers.put(ThxEntityHelicopter.class, new ThxRender());
        renderers.put(ThxEntityRocket.class, new ThxRender());
        renderers.put(ThxEntityMissile.class, new ThxRender());
    }
    
    @Override
    public String getVersion()
    {
        //log("getVersion called");
        return "Minecraft THX Helicopter Mod - mod_thx-mc146_v020_f";
    }
    
    int getNextItemId()
    {
        // return next available id
        for (int idx = 24100; idx + 256 < Item.itemsList.length; idx++)
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
        if (type == HELICOPTER_ENTITY_ID)
        {
            return new ThxEntityHelicopter(world, posX, posY, posZ, 0f);
        }
        
        if (type == ROCKET_ENTITY_ID)
        {
            return new ThxEntityRocket(world, posX, posY, posZ);
        }
        
        if (type == MISSILE_ENTITY_ID)
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
        
	    if (!"THX_entity".equals(packet.channel))
        {
            log("client received server update packet on unexpected channel: " + packet.channel);
	        return;
        }
	    
        ThxEntityPacket250Data data = new ThxEntityPacket250Data(packet);
        
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
        
	    if (!"THX_entity".equals(packet.channel))
        {
            log("server received client update packet on unexpected channel: " + packet.channel);
	        return;
        }
	    
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

}
