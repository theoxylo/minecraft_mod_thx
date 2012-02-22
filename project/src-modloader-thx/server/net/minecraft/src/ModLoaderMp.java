package net.minecraft.src;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;

public class ModLoaderMp
{
    public static final String NAME = "ModLoaderMP";
    public static final String VERSION = "1.1";
    private static boolean hasInit = false;
    private static Map entityTrackerMap = new HashMap();
    private static Map entityTrackerEntryMap = new HashMap();
    private static List bannedMods = new ArrayList();

    public ModLoaderMp()
    {
    }

    public static void InitModLoaderMp()
    {
        if (!hasInit)
        {
            init();
        }
    }

    // TODO: public static void RegisterEntityTracker(Class class1, int distance, int frequency, boolean updateMotion);
    
    public static void RegisterEntityTracker(Class class1, int distance, int frequency)
    {
        if (!hasInit)
        {
            init();
        }
        if (entityTrackerMap.containsKey(class1))
        {
            System.out.println("RegisterEntityTracker error: entityClass already registered.");
        }
        else
        {
            entityTrackerMap.put(class1, new Pair(Integer.valueOf(distance), Integer.valueOf(frequency)));
        }
    }

    public static void RegisterEntityTrackerEntry(Class entityClass, int netId)
    {
        RegisterEntityTrackerEntry(entityClass, false, netId);
    }

    public static void RegisterEntityTrackerEntry(Class entityClass, boolean entityHasOwner, int netId)
    {
        if (!hasInit)
        {
            init();
        }
        if (netId > 255)
        {
            System.out.println("RegisterEntityTrackerEntry error: entityId cannot be greater than 255.");
        }
        if (entityTrackerEntryMap.containsKey(entityClass))
        {
            System.out.println("RegisterEntityTrackerEntry error: entityClass already registered.");
        }
        else
        {
            entityTrackerEntryMap.put(entityClass, new EntityTrackerEntry2(netId, entityHasOwner));
        }
    }

    public static void HandleAllLogins(EntityPlayerMP entityplayermp)
    {
        if (!hasInit)
        {
            init();
        }
        sendModCheck(entityplayermp);
        for (int i = 0; i < ModLoader.getLoadedMods().size(); i++)
        {
            BaseMod basemod = (BaseMod)ModLoader.getLoadedMods().get(i);
            if (basemod instanceof BaseModMp)
            {
                ((BaseModMp)basemod).HandleLogin(entityplayermp);
            }
        }
    }

    public static void HandleAllPackets(Packet230ModLoader packet230modloader, EntityPlayerMP entityplayermp)
    {
        //Log("<<<<< HandleAllPackets - Packet230ModLoader received: " + packet230modloader);
        
        if (!hasInit)
        {
            init();
        }
        if (packet230modloader.modId == "ModLoaderMP".hashCode())
        {
            Log("Received 'ModLoaderMP' packet: " + packet230modloader);
            
            switch (packet230modloader.packetType)
            {
                case 0:
                    handleModCheckResponse(packet230modloader, entityplayermp);
                    break;

                case 1:
                    handleSendKey(packet230modloader, entityplayermp);
                    break;
            }
        }
        else
        {
            for (int i = 0; i < ModLoader.getLoadedMods().size(); i++)
            {
                BaseMod basemod = (BaseMod)ModLoader.getLoadedMods().get(i);
                if (!(basemod instanceof BaseModMp))
                {
                    continue;
                }
                BaseModMp basemodmp = (BaseModMp)basemod;
                if (basemodmp.getId() != packet230modloader.modId)
                {
                    continue;
                }
                
                //Log("Received custom mod packet: " + packet230modloader);
                
                basemodmp.HandlePacket(packet230modloader, entityplayermp);
                return;
            }
            
            Log("Received unrecognized packet: " + packet230modloader);
        }
    }

    public static void HandleEntityTrackers(EntityTracker entitytracker, Entity entity)
    {
        if (entity instanceof ThxEntity) Log("HandleEntityTrackers called for " + entity);
        
        if (!hasInit)
        {
            init();
        }
        for (Iterator iterator = entityTrackerMap.entrySet().iterator(); iterator.hasNext();)
        {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            if (((Class)entry.getKey()).isInstance(entity)) // handles subclasses and interfaces
            {
                boolean updateMotion = true; // TODO: allow this to be specified in call to RegisterEntityTracker above
                entitytracker.trackEntity(entity, ((Integer)((Pair)entry.getValue()).getLeft()).intValue(), ((Integer)((Pair)entry.getValue()).getRight()).intValue(), updateMotion);
                return;
            }
        }
    }

    public static EntityTrackerEntry2 HandleEntityTrackerEntries(Entity entity)
    {
        if (!hasInit)
        {
            init();
        }
        if (entityTrackerEntryMap.containsKey(entity.getClass()))
        {
            return (EntityTrackerEntry2)entityTrackerEntryMap.get(entity.getClass());
        }
        else
        {
            return null;
        }
    }

    public static void SendPacketToAll(BaseModMp basemodmp, Packet230ModLoader packet230modloader)
    {
        if (!hasInit)
        {
            init();
        }
        if (basemodmp == null)
        {
            IllegalArgumentException illegalargumentexception = new IllegalArgumentException("baseModMp cannot be null.");
            ModLoader.getLogger().throwing("ModLoaderMP", "SendPacketToAll", illegalargumentexception);
            ModLoader.ThrowException("baseModMp cannot be null.", illegalargumentexception);
            return;
        }
        else
        {
            packet230modloader.modId = basemodmp.getId();
            sendPacketToAll(packet230modloader);
            return;
        }
    }

    private static void sendPacketToAll(Packet packet)
    {
        //if (packet instanceof Packet230ModLoader) Log(">>>>> Sending packet to all players: " + packet);
        //else Log(">>>>> Sending non-ML packet to all players: " + packet);
        
        if (packet != null)
        {
            for (int i = 0; i < ModLoader.getMinecraftServerInstance().configManager.playerEntities.size(); i++)
            {
                ((EntityPlayerMP)ModLoader.getMinecraftServerInstance().configManager.playerEntities.get(i)).playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    public static void SendPacketTo(BaseModMp basemodmp, EntityPlayerMP entityplayermp, Packet230ModLoader packet230modloader)
    {
        if (!hasInit)
        {
            init();
        }
        if (basemodmp == null)
        {
            IllegalArgumentException illegalargumentexception = new IllegalArgumentException("baseModMp cannot be null.");
            ModLoader.getLogger().throwing("ModLoaderMP", "SendPacketTo", illegalargumentexception);
            ModLoader.ThrowException("baseModMp cannot be null.", illegalargumentexception);
            return;
        }
        else
        {
            packet230modloader.modId = basemodmp.getId();
            sendPacketTo(entityplayermp, packet230modloader);
            return;
        }
    }

    public static void Log(String s)
    {
        MinecraftServer.logger.info(s);
        ModLoader.getLogger().fine(s);
        //System.out.println(s);
    }

    public static World GetPlayerWorld(EntityPlayer entityplayer)
    {
        WorldServer aworldserver[] = ModLoader.getMinecraftServerInstance().worldMngr;
        for (int i = 0; i < aworldserver.length; i++)
        {
            if (((World) (aworldserver[i])).playerEntities.contains(entityplayer))
            {
                return aworldserver[i];
            }
        }

        return null;
    }

    private static void init()
    {
        hasInit = true;
        try
        {
            Method method;
            try
            {
                method = (net.minecraft.src.Packet.class).getDeclaredMethod("a", new Class[]
                        {
                            Integer.TYPE, Boolean.TYPE, Boolean.TYPE, java.lang.Class.class
                        });
            }
            catch (NoSuchMethodException nosuchmethodexception1)
            {
                method = (net.minecraft.src.Packet.class).getDeclaredMethod("addIdClassMapping", new Class[]
                        {
                            Integer.TYPE, Boolean.TYPE, Boolean.TYPE, java.lang.Class.class
                        });
            }
            method.setAccessible(true);
            method.invoke(null, new Object[]
                    {
                        Integer.valueOf(230), Boolean.valueOf(true), Boolean.valueOf(true), net.minecraft.src.Packet230ModLoader.class
                    });
        }
        catch (IllegalAccessException illegalaccessexception)
        {
            ModLoader.getLogger().throwing("ModLoaderMP", "AddCustomPacketMapping", illegalaccessexception);
            ModLoader.ThrowException("ModLoaderMP", illegalaccessexception);
            return;
        }
        catch (IllegalArgumentException illegalargumentexception)
        {
            ModLoader.getLogger().throwing("ModLoaderMP", "init", illegalargumentexception);
            ModLoader.ThrowException("ModLoaderMP", illegalargumentexception);
            return;
        }
        catch (InvocationTargetException invocationtargetexception)
        {
            ModLoader.getLogger().throwing("ModLoaderMP", "init", invocationtargetexception);
            ModLoader.ThrowException("ModLoaderMP", invocationtargetexception);
            return;
        }
        catch (NoSuchMethodException nosuchmethodexception)
        {
            ModLoader.getLogger().throwing("ModLoaderMP", "init", nosuchmethodexception);
            ModLoader.ThrowException("ModLoaderMP", nosuchmethodexception);
            return;
        }
        catch (SecurityException securityexception)
        {
            ModLoader.getLogger().throwing("ModLoaderMP", "init", securityexception);
            ModLoader.ThrowException("ModLoaderMP", securityexception);
            return;
        }
        try
        {
            File file = ModLoader.getMinecraftServerInstance().getFile("banned-mods.txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String s;
            while ((s = bufferedreader.readLine()) != null)
            {
                bannedMods.add(s);
            }
        }
        catch (FileNotFoundException filenotfoundexception)
        {
            ModLoader.getLogger().throwing("ModLoader", "init", filenotfoundexception);
            ModLoader.ThrowException("ModLoaderMultiplayer", filenotfoundexception);
            return;
        }
        catch (IOException ioexception)
        {
            ModLoader.getLogger().throwing("ModLoader", "init", ioexception);
            ModLoader.ThrowException("ModLoaderMultiplayer", ioexception);
            return;
        }
        Log("ModLoaderMP 1.1 Initialized");
    }

    private static void sendPacketTo(EntityPlayerMP entityplayermp, Packet230ModLoader packet230modloader)
    {
        //Log(">>>>> Sending packet to specific player: " + packet230modloader);
        
        entityplayermp.playerNetServerHandler.sendPacket(packet230modloader);
    }

    private static void sendModCheck(EntityPlayerMP entityplayermp)
    {
        Packet230ModLoader packet230modloader = new Packet230ModLoader();
        packet230modloader.modId = "ModLoaderMP".hashCode();
        packet230modloader.packetType = 0;
        sendPacketTo(entityplayermp, packet230modloader);
    }

    private static void handleModCheckResponse(Packet230ModLoader packet230modloader, EntityPlayerMP entityplayermp)
    {
        StringBuilder stringbuilder = new StringBuilder();
        if (packet230modloader.dataString.length != 0)
        {
            for (int i = 0; i < packet230modloader.dataString.length; i++)
            {
                if (packet230modloader.dataString[i].lastIndexOf("mod_") != -1)
                {
                    if (stringbuilder.length() != 0)
                    {
                        stringbuilder.append(", ");
                    }
                    stringbuilder.append(packet230modloader.dataString[i].substring(packet230modloader.dataString[i].lastIndexOf("mod_")));
                }
            }
        }
        else
        {
            stringbuilder.append("no mods");
        }
        Log((new StringBuilder()).append(entityplayermp.username).append(", ").append(entityplayermp).append(" ").append(entityplayermp.entityId).append(" joined with ").append(stringbuilder.toString()).toString());
        ArrayList arraylist = new ArrayList();
        for (int j = 0; j < bannedMods.size(); j++)
        {
            for (int k = 0; k < packet230modloader.dataString.length; k++)
            {
                if (packet230modloader.dataString[k].lastIndexOf("mod_") != -1 && packet230modloader.dataString[k].substring(packet230modloader.dataString[k].lastIndexOf("mod_")).startsWith((String)bannedMods.get(j)))
                {
                    arraylist.add(packet230modloader.dataString[k]);
                }
            }
        }

        ArrayList arraylist1 = new ArrayList();
        for (int l = 0; l < ModLoader.getLoadedMods().size(); l++)
        {
            BaseModMp basemodmp = (BaseModMp)ModLoader.getLoadedMods().get(l);
            if (!basemodmp.hasClientSide() || basemodmp.toString().lastIndexOf("mod_") == -1)
            {
                continue;
            }
            String s = basemodmp.toString().substring(basemodmp.toString().lastIndexOf("mod_"));
            boolean flag = false;
            for (int l1 = 0; l1 < packet230modloader.dataString.length; l1++)
            {
                if (packet230modloader.dataString[l1].lastIndexOf("mod_") == -1)
                {
                    continue;
                }
                String s1 = packet230modloader.dataString[l1].substring(packet230modloader.dataString[l1].lastIndexOf("mod_"));
                if (!s.equals(s1))
                {
                    continue;
                }
                flag = true;
                break;
            }

            if (!flag)
            {
                arraylist1.add(s);
            }
        }

        if (arraylist.size() != 0)
        {
            StringBuilder stringbuilder1 = new StringBuilder();
            for (int i1 = 0; i1 < arraylist.size(); i1++)
            {
                if (((String)arraylist.get(i1)).lastIndexOf("mod_") == -1)
                {
                    continue;
                }
                if (stringbuilder1.length() != 0)
                {
                    stringbuilder1.append(", ");
                }
                stringbuilder1.append(((String)arraylist.get(i1)).substring(((String)arraylist.get(i1)).lastIndexOf("mod_")));
            }

            Log((new StringBuilder()).append(entityplayermp.username).append(" kicked for having ").append(stringbuilder1.toString()).toString());
            StringBuilder stringbuilder3 = new StringBuilder();
            for (int k1 = 0; k1 < arraylist.size(); k1++)
            {
                if (((String)arraylist.get(k1)).lastIndexOf("mod_") != -1)
                {
                    stringbuilder3.append("\n");
                    stringbuilder3.append(((String)arraylist.get(k1)).substring(((String)arraylist.get(k1)).lastIndexOf("mod_")));
                }
            }

            entityplayermp.playerNetServerHandler.kickPlayer((new StringBuilder()).append("The following mods are banned on this server:").append(stringbuilder3.toString()).toString());
        }
        else if (arraylist1.size() != 0)
        {
            StringBuilder stringbuilder2 = new StringBuilder();
            for (int j1 = 0; j1 < arraylist1.size(); j1++)
            {
                if (((String)arraylist1.get(j1)).lastIndexOf("mod_") != -1)
                {
                    stringbuilder2.append("\n");
                    stringbuilder2.append(((String)arraylist1.get(j1)).substring(((String)arraylist1.get(j1)).lastIndexOf("mod_")));
                }
            }

            entityplayermp.playerNetServerHandler.kickPlayer((new StringBuilder()).append("You are missing the following mods:").append(stringbuilder2.toString()).toString());
        }
    }

    private static void handleSendKey(Packet230ModLoader packet230modloader, EntityPlayerMP entityplayermp)
    {
        if (packet230modloader.dataInt.length != 2)
        {
            System.out.println("SendKey packet received with missing data.");
        }
        else
        {
            int i = packet230modloader.dataInt[0];
            int j = packet230modloader.dataInt[1];
            for (int k = 0; k < ModLoader.getLoadedMods().size(); k++)
            {
                BaseMod basemod = (BaseMod)ModLoader.getLoadedMods().get(k);
                if (!(basemod instanceof BaseModMp))
                {
                    continue;
                }
                BaseModMp basemodmp = (BaseModMp)basemod;
                if (basemodmp.getId() != i)
                {
                    continue;
                }
                basemodmp.HandleSendKey(entityplayermp, j);
                break;
            }
        }
    }

    public static void getCommandInfo(ICommandListener icommandlistener)
    {
        for (int i = 0; i < ModLoader.getLoadedMods().size(); i++)
        {
            BaseMod basemod = (BaseMod)ModLoader.getLoadedMods().get(i);
            if (basemod instanceof BaseModMp)
            {
                BaseModMp basemodmp = (BaseModMp)basemod;
                basemodmp.GetCommandInfo(icommandlistener);
            }
        }
    }

    public static boolean HandleCommand(String s, String s1, Logger logger, boolean flag)
    {
        boolean flag1 = false;
        for (int i = 0; i < ModLoader.getLoadedMods().size(); i++)
        {
            BaseMod basemod = (BaseMod)ModLoader.getLoadedMods().get(i);
            if (!(basemod instanceof BaseModMp))
            {
                continue;
            }
            BaseModMp basemodmp = (BaseModMp)basemod;
            if (basemodmp.HandleCommand(s, s1, logger, flag))
            {
                flag1 = true;
            }
        }

        return flag1;
    }

    public static void sendChatToAll(String s, String s1)
    {
        String s2 = (new StringBuilder()).append(s).append(": ").append(s1).toString();
        sendChatToAll(s2);
    }

    public static void sendChatToAll(String s)
    {
        List list = ModLoader.getMinecraftServerInstance().configManager.playerEntities;
        for (int i = 0; i < list.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)list.get(i);
            entityplayermp.playerNetServerHandler.sendPacket(new Packet3Chat(s));
        }

        MinecraftServer.logger.info(s);
    }

    public static void sendChatToOps(String s, String s1)
    {
        String s2 = (new StringBuilder()).append("\2477(").append(s).append(": ").append(s1).append(")").toString();
        sendChatToOps(s2);
    }

    public static void sendChatToOps(String s)
    {
        List list = ModLoader.getMinecraftServerInstance().configManager.playerEntities;
        for (int i = 0; i < list.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)list.get(i);
            if (ModLoader.getMinecraftServerInstance().configManager.isOp(entityplayermp.username))
            {
                entityplayermp.playerNetServerHandler.sendPacket(new Packet3Chat(s));
            }
        }

        MinecraftServer.logger.info(s);
    }

    public static Packet GetTileEntityPacket(BaseModMp basemodmp, int i, int j, int k, int l, int ai[], float af[], String as[])
    {
        Packet230ModLoader packet230modloader = new Packet230ModLoader();
        packet230modloader.modId = "ModLoaderMP".hashCode();
        packet230modloader.packetType = 1;
        packet230modloader.isChunkDataPacket = true;
        int i1 = ai != null ? ai.length : 0;
        int ai1[] = new int[i1 + 5];
        ai1[0] = basemodmp.getId();
        ai1[1] = i;
        ai1[2] = j;
        ai1[3] = k;
        ai1[4] = l;
        if (i1 != 0)
        {
            System.arraycopy(ai, 0, ai1, 5, ai.length);
        }
        packet230modloader.dataInt = ai1;
        packet230modloader.dataFloat = af;
        packet230modloader.dataString = as;
        return packet230modloader;
    }

    public static void SendTileEntityPacket(TileEntity tileentity)
    {
        sendPacketToAll(tileentity.getDescriptionPacket());
    }

    public static BaseModMp GetModInstance(Class class1)
    {
        for (int i = 0; i < ModLoader.getLoadedMods().size(); i++)
        {
            BaseMod basemod = (BaseMod)ModLoader.getLoadedMods().get(i);
            if (!(basemod instanceof BaseModMp))
            {
                continue;
            }
            BaseModMp basemodmp = (BaseModMp)basemod;
            if (class1.isInstance(basemodmp))
            {
                return (BaseModMp)ModLoader.getLoadedMods().get(i);
            }
        }

        return null;
    }
}
