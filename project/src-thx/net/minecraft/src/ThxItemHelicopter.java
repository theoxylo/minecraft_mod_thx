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
    }

    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int i, int j, int k, int l)
    {
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
            
        ThxEntity te = new ThxEntityHelicopter(world, posX, posY, posZ, yaw);
        world.entityJoinedWorld(te);
        
        return false;
    }

    /*
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entity)
    {
        itemstack.stackSize--;
        world.playSoundAtEntity(entity, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        if(!world.multiplayerWorld)
        {
            float yawRad = entity.rotationYaw * RAD_PER_DEG;
            float pitchRad = entity.rotationPitch * RAD_PER_DEG;

            float f1 = MathHelper.cos(-yawRad - PI);
            float f3 = MathHelper.sin(-yawRad - PI);
            float f5 = -MathHelper.cos(-pitchRad);
            float f7 = MathHelper.sin(-pitchRad);
            double posX = entity.posX + f3 * f5 * 3.0;
            double posY = entity.posY + 1.0;
            double posZ = entity.posZ + f1 * f5 * 3.0;
            float yaw = (entity.rotationYaw -45) % 360f;
            
            ThxEntity te = new ThxEntityHelicopter(world, posX, posY, posZ, yaw);
            world.entityJoinedWorld(te);
        }
        return itemstack;
    }
    */
    
    /*
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
        float f = 1.0F;
        float f1 = entityplayer.prevRotationPitch + (entityplayer.rotationPitch - entityplayer.prevRotationPitch) * f;
        float f2 = entityplayer.prevRotationYaw + (entityplayer.rotationYaw - entityplayer.prevRotationYaw) * f;
        double d = entityplayer.prevPosX + (entityplayer.posX - entityplayer.prevPosX) * (double)f;
        double d1 = (entityplayer.prevPosY + (entityplayer.posY - entityplayer.prevPosY) * (double)f + 1.6200000000000001D) - (double)entityplayer.yOffset;
        double d2 = entityplayer.prevPosZ + (entityplayer.posZ - entityplayer.prevPosZ) * (double)f;
        Vec3D vec3d = Vec3D.createVector(d, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.01745329F - 3.141593F);
        float f4 = MathHelper.sin(-f2 * 0.01745329F - 3.141593F);
        float f5 = -MathHelper.cos(-f1 * 0.01745329F);
        float f6 = MathHelper.sin(-f1 * 0.01745329F);
        float f7 = f4 * f5;
        float f8 = f6;
        float f9 = f3 * f5;
        double d3 = 5D;
        Vec3D vec3d1 = vec3d.addVector((double)f7 * d3, (double)f8 * d3, (double)f9 * d3);
        MovingObjectPosition movingobjectposition = world.rayTraceBlocks_do(vec3d, vec3d1, true);
        if(movingobjectposition == null)
        {
            return itemstack;
        }
        if(movingobjectposition.typeOfHit == EnumMovingObjectType.TILE)
        {
            double x = (double)movingobjectposition.blockX + 0.5d;
            double y = (double)movingobjectposition.blockY + 1.5d;
            double z = (double)movingobjectposition.blockZ + 0.5d;
            if(!world.multiplayerWorld)
            {
                ThxEntity te = new ThxEntityHelicopter(world, x, y, z);
                world.entityJoinedWorld(te);
            }
            itemstack.stackSize--;
        }
        return itemstack;
    }
    */
}
