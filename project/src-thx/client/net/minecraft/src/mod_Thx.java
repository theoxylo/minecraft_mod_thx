package net.minecraft.src;

import net.minecraft.client.Minecraft;

import net.minecraft.src.forge.*;
import java.io.*;

public class mod_Thx extends NetworkMod implements IConnectionHandler, IPacketHandler
{
    static ThxConfig config;
    
    public static mod_Thx instance;
    public static final String channelName = "mod_Thx";

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
            MinecraftForge.registerEntity(ThxEntityRocket.class, this, 76, distance, frequency, sendVelocityInfo);
            
            int entityId = ModLoader.getUniqueEntityId();
            log("Registering entity class for Rocket with entity id " + entityId);
            ModLoader.registerEntityID(ThxEntityRocket.class, "thxRocket", entityId);
        }
        missile:
        {
            MinecraftForge.registerEntity(ThxEntityMissile.class, this, 77, distance, frequency, sendVelocityInfo);
            
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
        return "Minecraft THX Helicopter Mod - mod_thx-mc125_v018";
    }

    public void onPacketData(NetworkManager network, String channel, byte[] bytes) 
    {
        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(bytes));

        try
        {
            int packetType = dataStream.readInt(); // TODO
            int entityId = dataStream.readInt();
            
            if (entityId < 1) log("Received non-entity packet type " + packetType + ": " + entityId);
            else
            {
                Entity entity = ((WorldClient) ModLoader.getMinecraftInstance().theWorld).getEntityByID(entityId);
                
                // try calling applyUpdatePacket(packet);
                //if (entity instanceof ThxEntity) ((ThxEntity) entity).applyUpdatePacket(packet);
              
                // TODO: better abstraction; we don't really need to make a packet here to call ourselves
                Packet250CustomPayload packet = new Packet250CustomPayload();
                packet.channel = mod_Thx.channelName;
                packet.data = bytes;
                packet.length = packet.data.length;
                if (entity instanceof ThxEntity) ((ThxEntity) entity).lastUpdatePacket = packet;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
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
