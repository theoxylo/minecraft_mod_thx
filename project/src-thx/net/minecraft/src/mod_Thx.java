package net.minecraft.src;

public class mod_Thx extends BaseMod
{
	static Class classHelicopter = net.minecraft.src.ThxEntityHelicopter.class;
	static Class classMissile = net.minecraft.src.ThxEntityMissile.class;
    
    public mod_Thx()
    {
        ModLoader.SetInGameHook(this, true, true);
        
        // register entity classes
        missile:
        {
	        int entityId = ModLoader.getUniqueEntityId();
	        log("Registering entity class for missile with entity id " + entityId);
	        ModLoader.RegisterEntityID(classMissile, "thxMissile", entityId);
        }
        helicopter:
        {
	        int entityId = ModLoader.getUniqueEntityId();
	        log("Registering entity class for helicopter with entity id " + entityId);
	        ModLoader.RegisterEntityID(classHelicopter, "thxHelicopter", entityId);
        }
        
        helicopterItem:
        {
	        int itemId = getNextItemId();
	        log("Setting up inventory item for helicopter with item id " + itemId);
		    Item item = new ThxItemHelicopter(itemId);
	
		    item.setIconIndex(ModLoader.addOverride("/gui/items.png", "/thx/helicopter_icon.png"));
		    item.setItemName("thxHelicopter");
	        ModLoader.AddName(item, "THX Helicopter Prototype");
	        
	        log("Adding recipe for helicopter");
	        ItemStack itemStack = new ItemStack(item, 1, 1);
	        Object[] recipe = new Object[] {" X ", "X X", "XXX", Character.valueOf('X'), Block.planks};
	        ModLoader.AddRecipe(itemStack, recipe);
        }
        
        ThxConfig.loadProperties();
        
        log("Done loading " + Version());
    }

    @Override
    public void AddRenderer(java.util.Map map)
    {
        map.put(classHelicopter, new ThxRender());
        map.put(classMissile, new ThxRender());
    }

    @Override
    public String Version()
    {
        return "mod_thx-beta_1.6.6_v005";
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
    
    void log(String s) 
    { 
    	System.out.println("mod_thx: " + s); 
	}
}
