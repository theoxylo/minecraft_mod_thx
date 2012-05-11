package net.minecraft.src;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import net.minecraft.src.forge.*;

public class mod_Thx extends NetworkMod implements IConnectionHandler, IPacketHandler
{
    static ThxConfig config;
    
    public static mod_Thx instance;
    
    public static final String channelName = "mod_Thx";
    public static Logger logger = Logger.getLogger("thxLog"); 

    public mod_Thx()
    {
        /* java.util.logging approach...
        try
        {
            Handler handler = new FileHandler("mods/mod_thx.log");
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
            
	        String level = ThxConfig.getProperty("enable_logging_level", "SEVERE");
	        System.out.println("thxLog.level: " + level);
	        logger.setLevel(Level.parse(level));
        }
        catch (Exception e)
        {
            System.out.println("Could not open log file 'mods/mod_thx.log': " + e);
        }
        logger.fine("log fine test");
        logger.info("log info test");
        logger.warning("log warning test");
        */
        
        //log("mod_Thx() called");
        System.out.println("mod_Thx() called");
        
		config = new ThxConfig();

        instance = this; // for easy access by static methods and to instance methods
    }

    @Override
    public void load()
    {
        log("load() called");

        //ThxConfig.loadProperties();

        ModLoader.setInGameHook(this, true, true);

        log("enabling flight on server");
        ModLoader.getMinecraftServerInstance().allowFlight = true;
        log("server.allowFlight: " + ModLoader.getMinecraftServerInstance().allowFlight);

        int distance = 160; // spawn/despawn at this distance from entity
        int frequency = 1; // ticks per update, 1 to 60 (20 ticks/sec)
        boolean sendVelocityInfo = true;
        
        // register entity classes
        helicopter:
        {
            MinecraftForge.registerEntity(ThxEntityHelicopter.class, this, 75, distance, frequency, sendVelocityInfo);

            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Helicopter with entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityHelicopter.class, "thxHelicopter", entityId);
        }
        rocket:
        {
            boolean hasOwner = true;
            MinecraftForge.registerEntity(ThxEntityRocket.class, this, 76, /*distance*/ 100, /*frequency*/ 4, sendVelocityInfo);
            
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Rocket with entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityMissile.class, "thxRocket", entityId);
        }
        missile:
        {
            MinecraftForge.registerEntity(ThxEntityMissile.class, this, 77, distance, /*frequency*/ 4, sendVelocityInfo);
            
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
        return "Minecraft THX Helicopter Mod - mod_thx-mc125_v018";
    }

    // TODO: register channel!
    public void onPacketData(NetworkManager network, String channel, byte[] bytes)
    {
        EntityPlayer player = ((NetServerHandler)network.getNetHandler()).getPlayerEntity();

        if (player.ridingEntity instanceof IClientDriven)
        {
            // try calling applyUpdatePacket(packet);

            // TODO: better abstraction; we don't really need to make a packet here to call ourselves
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = mod_Thx.channelName;
            packet.data = bytes;
            packet.length = packet.data.length;

            ((ThxEntity) player.ridingEntity).applyUpdatePacket(packet);
            //((ThxEntity) player.ridingEntity).latestUpdatePacket = packet;
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

    public void onLogin(NetworkManager network, Packet1Login login)
    {
        MessageManager.getInstance().registerChannel(network, this, channelName);
    }

    public void onConnect(NetworkManager network){}
    public void onDisconnect(NetworkManager network, String message, Object[] args) {}
    public boolean clientSideRequired()
    {
        return true;
    }
    public boolean serverSideRequired()
    {
        return false;
    }

}
