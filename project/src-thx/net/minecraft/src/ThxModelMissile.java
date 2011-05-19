package net.minecraft.src;

public class ThxModelMissile extends ModelBase
{
    public ModelRenderer box;
    
    public ThxModelMissile()
    {
        float length = 16f;
        float width  = 4f;
        float height = 4f;

        box = new ModelRenderer(0, 0);
        box.addBox(-length/2f, -width/2f, -height/2f, (int)length, (int)width, (int)height);
        box.setPosition(0f, 0f, 0f);
    }

    @Override
    public void render(float f, float f1, float f2, float f3, float f4, float f5)
    {
        box.render(f5);
    }
}
