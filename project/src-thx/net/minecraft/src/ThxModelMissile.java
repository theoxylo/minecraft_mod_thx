package net.minecraft.src;

public class ThxModelMissile extends ThxModel
{
    public ModelRenderer missile;
    
    public ThxModelMissile()
    {       
        float x_length = 16f;
        float z_width  = 4f;
        float y_height = 4f;

        missile = new ModelRenderer(0, 0);
        missile.addBox(-x_length/2f, -y_height/2f, -z_width/2f, (int)x_length, (int)y_height, (int)z_width);
        missile.setRotationPoint(0f, 0f, 0f);
    }

    @Override
    public void render(float f, float f1, float f2, float f3, float f4, float f5)
    {
        updateDeltaTime();
        
        if (!visible) return;
        
        // spiral
        missile.rotateAngleX += deltaTime * 5f; // radians per sec
        if (missile.rotateAngleX > 2*PI) missile.rotateAngleX -= 2*PI;
        
        missile.render(f5);
    }
}
