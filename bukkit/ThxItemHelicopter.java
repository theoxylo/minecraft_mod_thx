package net.minecraft.server;

public class ThxItemHelicopter extends Item
{
    final float RAD_PER_DEG = 0.01745329F;
    final float PI = (float)Math.PI;
    public static int shiftedId;

    public ThxItemHelicopter(int var1)
    {
        super(var1);
        this.maxStackSize = 16;
        shiftedId = this.id;
        mod_Thx.log("Created new helicopter item: " + this);
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS !
     */
    public boolean interactWith(ItemStack var1, EntityHuman var2, World var3, int var4, int var5, int var6, int var7)
    {
        mod_Thx.log("onItemUse - using helicopter item: " + this);
        --var1.count;
        var3.makeSound(var2, "random.bow", 0.5F, 0.4F / (c.nextFloat() * 0.4F + 0.8F));
        float var8 = var2.yaw * 0.01745329F;
        float var9 = var2.pitch * 0.01745329F;
        float var10 = MathHelper.cos(-var8 - (float)Math.PI);
        float var11 = MathHelper.sin(-var8 - (float)Math.PI);
        float var12 = -MathHelper.cos(-var9);
        float var13 = MathHelper.sin(-var9);
        double var14 = var2.locX + (double)(var11 * var12) * 3.0D;
        double var16 = var2.locY + 1.0D;
        double var18 = var2.locZ + (double)(var10 * var12) * 3.0D;
        float var20 = (var2.yaw - 45.0F) % 360.0F;

        if (var3.isStatic)
        {
            mod_Thx.log(this + ".onItemUse: world is remote, so no direct call to spawn");
        }
        else
        {
            mod_Thx.log(this + ".onItemUse: Spawning new helicopter from item");
            ThxEntityHelicopter var21 = new ThxEntityHelicopter(var3, var14, var16, var18, var20);
            var21.owner = var2;
            var3.addEntity(var21);
        }

        return false;
    }
}
