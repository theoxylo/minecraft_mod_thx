package net.minecraft.src;


public class ThxModelMissile extends ThxModel
{
    public ModelRenderer missile;
    
    float rollRadPerSec = 5f;
    
    public ThxModelMissile()
    {       
        //renderTexture = "textures/entity/missile.png";
        renderTexture = ThxConfig.getProperty("texture_missile");

        float x_length = 16f;
        float z_width  = 4f;
        float y_height = 4f;

        missile = new ModelRenderer(this, 0, 0);
        missile.addBox(-x_length/2f, -y_height/2f, -z_width/2f, (int)x_length, (int)y_height, (int)z_width);
        missile.setRotationPoint(0f, 0f, 0f);
    }

    public void render()
    {
        update();
        
        if (!visible) return;
        
        // spiral
        missile.rotateAngleX += deltaTime * rollRadPerSec;
        if (missile.rotateAngleX > 2*PI) missile.rotateAngleX -= 2*PI;
        
        //float scale = .0625f; // original size
        //float scale = .04f;
        //missile.render(scale);
        missile.render(.04f);
    }
}
