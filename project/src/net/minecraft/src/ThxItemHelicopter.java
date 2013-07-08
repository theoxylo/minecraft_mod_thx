package net.minecraft.src;

public class ThxItemHelicopter extends Item
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;
   
    public static int shiftedId;
    
    public ThxItemHelicopter(int id)
    {
        super(id);
        shiftedId = itemID;
        
        //setItemName("thxHelicopterItem"); // mc_147
        setUnlocalizedName("helicopter_icon"); // mc_150, mapped to textures/items/helicopter_icon.png
        setMaxStackSize(16);
        
        setMaxDamage(0);
        setCreativeTab(CreativeTabs.tabTransport);
        
        // required to locate png file in thx.zip/assets/mod_thx/textures/items/helicopter_icon.png
        func_111206_d("helicopter_icon");
        
        ThxConfig.log("Created new helicopter item with id " + id + ": " + this);
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
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
            mod_thx.log(this + ".onItemRightClick: world is remote, so no direct call to spawn");
        }
        else
        {
            mod_thx.log(this + ".onItemRightClick: Spawning new helicopter from item");
            
            ThxEntityHelicopter newHelicopter = new ThxEntityHelicopter(world, posX, posY, posZ, yaw);
            newHelicopter.owner = player;
            world.spawnEntityInWorld(newHelicopter);
        }
        
        return itemstack;
    }
}
