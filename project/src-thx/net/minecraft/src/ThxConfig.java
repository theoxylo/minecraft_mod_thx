package net.minecraft.src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;

public class ThxConfig
{
    static boolean ENABLE_LOGGING;
    static String filename = Minecraft.getMinecraftDir() + "/mods/mod_thx.options";
    static Properties props;
    
    /* troubleshooting this startup error:
     * 
     * 
		[19:06] java.lang.Exception: new ThxEntityMissile called
		[19:06] at net.minecraft.src.ThxEntityMissile.<init>(ThxEntityMissile.java:47)
		[19:06] at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
		[19:06] at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
		[19:06] at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
		[19:06] at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
		[19:06] at net.minecraft.src.EntityList.createEntityFromNBT(EntityList.java:66)
		[19:06] at net.minecraft.src.ChunkLoader.loadChunkIntoWorldFromCompound(ChunkLoader.java:212)
		[19:06] at net.minecraft.src.McRegionChunkLoader.loadChunk(McRegionChunkLoader.java:43)
		[19:06] at net.minecraft.src.ChunkProviderLoadOrGenerate.func_542_c(ChunkProviderLoadOrGenerate.java:131)
		[19:06] at net.minecraft.src.ChunkProviderLoadOrGenerate.provideChunk(ChunkProviderLoadOrGenerate.java:82)
		[19:06] at net.minecraft.src.World.getChunkFromChunkCoords(World.java:370)
		[19:06] at net.minecraft.src.World.getBlockId(World.java:306)
		[19:06] at net.minecraft.client.Minecraft.func_6255_d(Minecraft.java:1342)
		[19:06] at net.minecraft.client.Minecraft.changeWorld(Minecraft.java:1265)
		[19:06] at net.minecraft.client.Minecraft.changeWorld2(Minecraft.java:1229)
		[19:06] at net.minecraft.client.Minecraft.startWorld(Minecraft.java:1178)
		[19:06] at net.minecraft.src.GuiSelectWorld.selectWorld(GuiSelectWorld.java:133)
		[19:06] at net.minecraft.src.GuiSelectWorld.actionPerformed(GuiSelectWorld.java:100)
		[19:06] at net.minecraft.src.GuiScreen.mouseClicked(GuiScreen.java:76)
		[19:06] at net.minecraft.src.GuiScreen.handleMouseInput(GuiScreen.java:123)
		[19:06] at net.minecraft.src.GuiScreen.handleInput(GuiScreen.java:113)
		[19:06] at net.minecraft.client.Minecraft.runTick(Minecraft.java:941)
		[19:06] at net.minecraft.client.Minecraft.run(Minecraft.java:451)
		[19:06] at java.lang.Thread.run(Thread.java:662)
		[19:06] Wrong location! Missile 0
		[19:06] java.lang.Exception: Stack trace
		[19:06] at java.lang.Thread.dumpStack(Thread.java:1249)
		[19:06] at net.minecraft.src.Chunk.addEntity(Chunk.java:389)
		[19:06] at net.minecraft.src.ChunkLoader.loadChunkIntoWorldFromCompound(ChunkLoader.java:216)
		[19:06] at net.minecraft.src.McRegionChunkLoader.loadChunk(McRegionChunkLoader.java:43)
		[19:06] at net.minecraft.src.ChunkProviderLoadOrGenerate.func_542_c(ChunkProviderLoadOrGenerate.java:131)
		[19:06] at net.minecraft.src.ChunkProviderLoadOrGenerate.provideChunk(ChunkProviderLoadOrGenerate.java:82)
		[19:06] at net.minecraft.src.World.getChunkFromChunkCoords(World.java:370)
		[19:06] at net.minecraft.src.World.getBlockId(World.java:306)
		[19:06] at net.minecraft.client.Minecraft.func_6255_d(Minecraft.java:1342)
		[19:06] at net.minecraft.client.Minecraft.changeWorld(Minecraft.java:1265)
		[19:06] at net.minecraft.client.Minecraft.changeWorld2(Minecraft.java:1229)
		[19:06] at net.minecraft.client.Minecraft.startWorld(Minecraft.java:1178)
		[19:06] at net.minecraft.src.GuiSelectWorld.selectWorld(GuiSelectWorld.java:133)
		[19:06] at net.minecraft.src.GuiSelectWorld.actionPerformed(GuiSelectWorld.java:100)
		[19:06] at net.minecraft.src.GuiScreen.mouseClicked(GuiScreen.java:76)
		[19:06] at net.minecraft.src.GuiScreen.handleMouseInput(GuiScreen.java:123)
		[19:06] at net.minecraft.src.GuiScreen.handleInput(GuiScreen.java:113)
		[19:06] at net.minecraft.client.Minecraft.runTick(Minecraft.java:941)
		[19:06] at net.minecraft.client.Minecraft.run(Minecraft.java:451)
		[19:06] at java.lang.Thread.run(Thread.java:662)
		
	// some test code	
    static EntityBoat boat;
    static Chunk chunk;
    static EntityList entities;
    static
    {
	    if (chunk != null) chunk.addEntity(null);
	    if (entities != null) entities.createEntityFromNBT(null, null);
	    if (boat != null) boat.writeEntityToNBT(null);
    }
     */
    
    

    
    static String getProperty(String name)
    {
        if (props == null)
        {
            loadProperties();
        }
        return props.getProperty(name);
    }
    
    static int getIntProperty(String name)
    {
        try
        {
            return Integer.parseInt(getProperty(name));
        }
        catch (Exception e)
        {
            System.out.println("Error loading int property '" + name + "'");
            return 0;
        }
    }
    
    static boolean getBoolProperty(String name)
    {
        try
        {
            return Boolean.parseBoolean(getProperty(name));
        }
        catch (Exception e)
        {
            System.out.println("Error loading boolean property '" + name + "'");
            return false;
        }
    }
    
    static void loadProperties()
    {
        boolean writeFile = false;
        
        props = new Properties();
        try
        {
            props.load(new FileInputStream(filename));
        }
        catch(FileNotFoundException ioe1)
        {
            writeFile = true;
        }
        catch (Exception e)
        {
            System.out.println("Error loading properties: " + e);
        }
        
        // add any missing properties using default values
        writeFile = ensureDefault(props, "enable_logging", "false") || writeFile;
        writeFile = ensureDefault(props, "ascend", Keyboard.getKeyName(Keyboard.KEY_SPACE)) || writeFile;
        writeFile = ensureDefault(props, "descend", Keyboard.getKeyName(Keyboard.KEY_X)) || writeFile;
        writeFile = ensureDefault(props, "forward", Keyboard.getKeyName(Keyboard.KEY_W)) || writeFile;
        writeFile = ensureDefault(props, "back", Keyboard.getKeyName(Keyboard.KEY_S)) || writeFile;
        writeFile = ensureDefault(props, "left", Keyboard.getKeyName(Keyboard.KEY_A)) || writeFile;
        writeFile = ensureDefault(props, "right", Keyboard.getKeyName(Keyboard.KEY_D)) || writeFile;
        writeFile = ensureDefault(props, "rotate_left", Keyboard.getKeyName(Keyboard.KEY_G)) || writeFile;
        writeFile = ensureDefault(props, "rotate_right", Keyboard.getKeyName(Keyboard.KEY_H)) || writeFile;
        writeFile = ensureDefault(props, "key_fire_missile", Keyboard.getKeyName(Keyboard.KEY_M)) || writeFile;
        writeFile = ensureDefault(props, "key_enter_exit", Keyboard.getKeyName(Keyboard.KEY_Y)) || writeFile;
        writeFile = ensureDefault(props, "enable_look_yaw", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_look_pitch", "false") || writeFile;
        writeFile = ensureDefault(props, "enable_drone_mode", "false") || writeFile;
        writeFile = ensureDefault(props, "enable_pilot_aim", "false") || writeFile;
        writeFile = ensureDefault(props, "enable_rotor", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_auto_level", "true") || writeFile;
        writeFile = ensureDefault(props, "rotor_speed_percent", "30") || writeFile;
        
        System.out.println("Loaded properties: " + props);
        
        if (writeFile) // update file with defaults if any properties were missing
        {
	        try
	        {
	            // create default properties file
	            System.out.println("Creating/updating config file " + filename);
	            props.store(new FileOutputStream(filename), "Added default properties");
	        }
	        catch(IOException e) { System.out.println("Error writing default properties file: " + e); }
        }
        
        ENABLE_LOGGING = getBoolProperty("enable_logging");
    }
    
    static boolean ensureDefault(Properties props, String name, String defaultValue)
    {
        if (props.getProperty(name) == null)
        {
            props.setProperty(name, defaultValue);
            return true;
        }
        return false;
    }
    
    static void logKeyMap()
    {
        log("Key names:");
        for (int i = 0; i < 200; i++)
        {
            log("Key " + i + ": " + Keyboard.getKeyName(i));
        }
    }
    
    static void log(String s)
    {
        if (ENABLE_LOGGING) System.out.println(new java.util.Date() + ": " + s);
    }
}
