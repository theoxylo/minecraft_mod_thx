package net.minecraft.src;

public class ThxModelMissile extends ThxModel
{
    public ModelRenderer box;
    
    public ThxModelMissile()
    {       
        float x_length = 16f;
        float z_width  = 4f;
        float y_height = 4f;

        box = new ModelRenderer(0, 0);
        box.addBox(-x_length/2f, -z_width/2f, -y_height/2f, (int)x_length, (int)z_width, (int)y_height);
        box.setRotationPoint(0f, 0f, 0f);
    }

    @Override
    public void render(float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(f, f1, f2, f3, f4, f5);
        
        if (!visible) return;
        
        // spiral
        box.rotateAngleX += deltaTime * 5f; // per sec
        
        box.render(f5);
    }
}
