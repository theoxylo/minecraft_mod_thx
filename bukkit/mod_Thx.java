package net.minecraft.server;

import forge.IConnectionHandler;
import forge.IPacketHandler;
import forge.MessageManager;
import forge.MinecraftForge;
import forge.NetworkMod;
import java.util.logging.Logger;

public class mod_Thx extends NetworkMod implements IConnectionHandler, IPacketHandler
{
    static ThxConfig config;
    public static mod_Thx instance;
    public static final String channelName = "mod_Thx";
    public static Logger logger = Logger.getLogger("thxLog");

    public static ItemStack helicopterItemStack;

    public mod_Thx()
    {
        System.out.println("mod_Thx() called");
        config = new ThxConfig();
        instance = this;
    }

    public void load()
    {
        log("load() called");
        MinecraftForge.registerConnectionHandler(this);
        ModLoader.setInGameHook(this, true, true);
        log("enabling flight on server");
        ModLoader.getMinecraftServerInstance().allowFlight = true;
        log("server.allowFlight: " + ModLoader.getMinecraftServerInstance().allowFlight);
        short var1 = 160;
        byte var2 = 1;
        boolean var3 = true;
        MinecraftForge.registerEntity(ThxEntityHelicopter.class, this, 75, var1, var2, var3);
        int var4 = ModLoader.getUniqueEntityId();
        log("Registering entity class for Helicopter with entity id " + var4);
        ModLoader.registerEntityID(ThxEntityHelicopter.class, "thxHelicopter", var4);
        boolean var8 = true;
        MinecraftForge.registerEntity(ThxEntityRocket.class, this, 76, 100, 4, var3);
        int var5 = ModLoader.getUniqueEntityId();
        log("Registering entity class for Rocket with entity id " + var5);
        ModLoader.registerEntityID(ThxEntityMissile.class, "thxRocket", var5);
        MinecraftForge.registerEntity(ThxEntityMissile.class, this, 77, var1, 4, var3);
        var4 = ModLoader.getUniqueEntityId();
        log("Registering entity class for Missile with entity id " + var4);
        ModLoader.registerEntityID(ThxEntityMissile.class, "thxMissile", var4);
        var4 = this.getNextItemId();
        log("Setting up inventory item for helicopter with item id " + var4);
        ThxItemHelicopter var9 = new ThxItemHelicopter(var4);

        if (config.getBoolProperty("disable_helicopter_item_image"))
        {
            var9.d(92);
        }
        else
        {
            var9.d(ModLoader.addOverride("/gui/items.png", "/thx/helicopter_icon.png"));
        }

        var9.a("thxHelicopter");
        helicopterItemStack = new ItemStack(var9, 1);
        log("Done loading " + this.getVersion());
    }

    public void modsLoaded()
    {
        config.addHelicopterRecipe(helicopterItemStack);
    }

    public String getVersion()
    {
        return "Minecraft THX Helicopter Mod - mod_thx-mc125_v018";
    }

    public void onPacketData(NetworkManager var1, String var2, byte[] var3)
    {
        EntityPlayer var4 = ((NetServerHandler)var1.getNetHandler()).getPlayerEntity();

        if (var4.vehicle instanceof IClientDriven)
        {
            Packet250CustomPayload var5 = new Packet250CustomPayload();
            var5.tag = "mod_Thx";
            var5.data = var3;
            var5.length = var5.data.length;
            ((ThxEntity)var4.vehicle).applyUpdatePacket(var5);
        }
    }

    int getNextItemId()
    {
        for (int var1 = 24000; var1 + 256 < Item.byId.length; ++var1)
        {
            if (Item.byId[var1 + 256] == null)
            {
                return var1;
            }
        }

        throw new RuntimeException("Could not find next available Item ID -- can\'t continue!");
    }

    public static void log(String var0)
    {
        if (ThxConfig.ENABLE_LOGGING)
        {
            System.out.println("mod_thx: " + var0);
        }
    }

    static String getProperty(String var0)
    {
        return config.getProperty(var0);
    }

    static int getIntProperty(String var0)
    {
        return config.getIntProperty(var0);
    }

    static boolean getBoolProperty(String var0)
    {
        return config.getBoolProperty(var0);
    }

    public void onLogin(NetworkManager var1, Packet1Login var2)
    {
        MessageManager.getInstance().registerChannel(var1, this, "mod_Thx");
    }

    public void onConnect(NetworkManager var1) {}

    public void onDisconnect(NetworkManager var1, String var2, Object[] var3) {}

    public boolean clientSideRequired()
    {
        return true;
    }

    public boolean serverSideRequired()
    {
        return false;
    }
}
