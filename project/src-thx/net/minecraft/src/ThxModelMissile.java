package net.minecraft.src;

public class ThxModelMissile extends ModelBase
{
    public ModelRenderer box;
    
    public ThxModelMissile()
    {
        box = new ModelRenderer(0, 0);
        box.addBox(-8, -2, -2, 16, 4, 4, 0.0F);
        box.setPosition(0f, 0f, 0f);
        //box.rotateAngleX = 1.570796F;
    }

    @Override
    public void render(float f, float f1, float f2, float f3, float f4, float f5)
    {
        box.render(f5);
    }
}
