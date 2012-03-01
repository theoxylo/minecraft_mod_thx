package net.minecraft.src;

public class ThxItemHelicopter extends Item
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;
   
    public static int shiftedId;
    
    public ThxItemHelicopter(int i)
    {
        super(i);
        maxStackSize = 16;
        shiftedId = shiftedIndex;
        
        mod_Thx.log("Created new helicopter item: " + this);
    }

    @Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int i, int j, int k, int l)
    {
        mod_Thx.log("onItemUse - using helicopter item: " + this);
        
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
            mod_Thx.log(this + ".onItemUse: world is remote, so no direct call to spawn");
        }
        else
        {
            mod_Thx.log(this + ".onItemUse: Spawning new helicopter from item");
            world.spawnEntityInWorld(new ThxEntityHelicopter(world, posX, posY, posZ, yaw));;
        }
        
        return false;
    }

}
