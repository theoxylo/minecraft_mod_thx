package net.minecraft.src;

public class mod_Thx_server extends BaseModMp
{
    public void ModsLoaded()
    {
        load();
    }
    
	@Override
	public void load()
    {
        log("Done loading " + getVersion());
    }
    

    public String Version()
    {
        return getVersion();
    }
    
    @Override
    public String getVersion()
    {
        return "Minecraft Helicopter Mod - mod_thx-mc110_v016_server";
    }
    
    void log(String s) 
    { 
    	System.out.println("mod_thx_server: " + s); 
	}
}
