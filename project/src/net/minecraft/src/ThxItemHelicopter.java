package net.minecraft.src;


public class ThxItemHelicopter extends Item
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;
   
    public static int shiftedId;
    
    public ThxItemHelicopter(int id)
    {
        super(id);
        shiftedId = shiftedIndex;
        
        // not working in Forge
        //setTextureFile("/thx/helicopter_icon.png");
        
        setItemName("thxHelicopterItem");
        setMaxStackSize(16);
        //setIconIndex(0);
        setIconIndex(ModLoader.addOverride("/gui/items.png", "/thx/helicopter_icon.png"));
        setMaxDamage(0);
        setCreativeTab(CreativeTabs.tabTransport);
        
        ThxConfig.log("Created new helicopter item with id " + id + ": " + this);
    }
    
    // setTextureFile above is not working after reobfuscation
    /*
    @Override
    public String getTextureFile()
    {
        return "/thx/helicopter_icon.png";
    }
    */

    /*
    @Override
    public Item setTextureFile(String texture)
    {
        return super.setTextureFile(texture);
    }
    */
    
    @Override
    //public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int i, int j, int k, int l)
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        //mod_thx.log("onItemUse - using helicopter item: " + this + " by player: " + player);
        mod_thx.log("onItemUse - using helicopter item: " + this + " by player: " + player);
        
        itemstack.stackSize--;
        
        world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        
        float yawRad = player.rotationYaw * RAD_PER_DEG;
        float pitchRad = player.rotationPitch * RAD_PER_DEG;

        float f1 = MathHelper.cos(-yawRad - PI);
        float f3 = MathHelper.sin(-yawRad - PI);
        float f5 = -MathHelper.cos(-pitchRad);
        float f7 = MathHelper.sin(-pitchRad);
        double posX = player.posX + f3 * f5 * 3.0;
        double posY = player.posY + 1.0;
        double posZ = player.posZ + f1 * f5 * 3.0;
        float yaw = (player.rotationYaw -45) % 360f;
            
        if (world.isRemote)
        {
            //mod_thx.log(this + ".onItemUse: world is remote, so no direct call to spawn");
            mod_thx.log(this + ".onItemRightClick: world is remote, so no direct call to spawn");
        }
        else
        {
            //mod_thx.log(this + ".onItemUse: Spawning new helicopter from item");
            mod_thx.log(this + ".onItemRightClick: Spawning new helicopter from item");
            
            ThxEntityHelicopter newHelicopter = new ThxEntityHelicopter(world, posX, posY, posZ, yaw);
            //ThxEntity newHelicopter = new ThxEntityMissile(world, posX, posY, posZ, yaw);
            newHelicopter.owner = player;
            world.spawnEntityInWorld(newHelicopter);
        }
        
        return itemstack;
    }

}
