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

    public void render()
    {
        update();
        
        if (!visible) return;
        
        // spiral
        missile.rotateAngleX += deltaTime * 5f; // radians per sec
        if (missile.rotateAngleX > 2*PI) missile.rotateAngleX -= 2*PI;
        
        //float scale = .0625f; // original size
        //float scale = .04f;
        //missile.render(scale);
        missile.render(.04f);
    }
}
