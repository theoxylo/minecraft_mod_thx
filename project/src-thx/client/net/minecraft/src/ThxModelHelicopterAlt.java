package net.minecraft.src;

public class ThxModelHelicopterAlt extends ThxModelHelicopter
{
    public ModelRenderer body1;
    public ModelRenderer body2;
    
    public ThxModelHelicopterAlt()
    {
        renderTexture = "/thx/helicopter-alt.png";

        // new body, large enough to hide player model in 3rd person view.
        // in 1st person view, it is invisible
        float length = 16f;
        float height = 4f;
        float width  = 4f;
        float heightOffset = -9f; //9 units off ground
        
        body1 = new ModelRenderer(this, 8, 2); // texture start at upper-left pixel (8,4) of png
        body1.setRotationPoint(0f, 0f + heightOffset, 8f); // 8 units right
        body1.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width); // box is centered at rotation point)
        body1.rotateAngleZ = -.3f; // ~20 degrees pitch forward
        
        length = 16f;
        height = 8f;
        width  = 6f;
        
        body2 = new ModelRenderer(this, 6, 12);
        body2.setRotationPoint(0f, 0f + heightOffset, -8f); // 8 units left
        body2.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
        
        //body2.rotateAngleZ = -.7f; // Pitch rad, ~40 degrees pitch forward or down
        //body2.rotateAngleY = -1.5f; // Yaw rad, ~90 degrees yaw left (counter-clockwise)
        //body2.rotateAngleX = -.3f; // Roll rad,
    }

    public void render()
    {
        update();
        
        //float scale = 0.0625f;
        float scale = 0.07f;
        
        //System.out.println("Model delta time sec: " + deltaTime);
        
        if (!visible) return;
        
        body1.render(scale);
        
        body2.rotateAngleX += deltaTime * .4f; // slow roll
        body2.rotateAngleY += deltaTime * .4f; // slow yaw
        body2.rotateAngleZ += deltaTime * .4f; // slow pitch
        body2.render(scale);
    }
}
