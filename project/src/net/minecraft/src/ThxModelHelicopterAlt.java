package net.minecraft.src;


public class ThxModelHelicopterAlt extends ThxModelHelicopterBase
{
    public ModelRenderer body;
    public ModelRenderer mainRotor;
    public ModelRenderer tailRotor;
    public ModelRenderer windshield;
    
    public ThxModelHelicopterAlt()
    {
        renderTexture = "/thx/helicopter-alt.png";

        body:
        {
            // will be rendered at increased scale --
            // large enough to hide player in 3rd person view.
            // in 1st person view, body is invisible and only
            // windshield and rotors are rendered
            
            float length = 12f; // units are pixels in a 64x32 texture
            float height =  8f;
            float width  =  6f;
            body = new ModelRenderer(this, 0, 12);
            body.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            body.setRotationPoint(0f, -2f, 0f); // move up
        }
        mainRotor: 
        {
            float length = 30f;
            float height =  0f;
            float width  =  1f;
            mainRotor = new ModelRenderer(this, 0, 0);
            mainRotor.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            mainRotor.setRotationPoint(2f, -11.7f, 0f); // halved to adjust for scale, and a little subtracted 
        }
        tailRotor:
        {
            float length = 8f;
            float height = 1f;
            float width  = 0f;
            tailRotor = new ModelRenderer(this, 0, 2);
            tailRotor.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            tailRotor.setRotationPoint(16f, -7f, .7f);
        }
        windshield:
        {
            float length = 9f;
            float height = 7f;
            float width  = 0f;
            windshield = new ModelRenderer(this, 0, 4);
            windshield.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            windshield.setRotationPoint(-6.5f, -5.5f, 0f);
            windshield.rotateAngleY = PI * 1.5f; // rotate 270 deg yaw for proper orientation
        }
    }

    public void render()
    {
        super.render();
        
        if (!visible) return;
        
        float scale = 0.125f;
    
        body.render(scale * 2f);
        
        mainRotor.rotateAngleY = mainRotorAngle;
        mainRotor.render(scale * 1.3f);
        // rotate 1/4 turn and render again
        mainRotor.rotateAngleY += 1.5707f; 
        mainRotor.render(scale * 1.3f);
        
        tailRotor.rotateAngleZ = tailRotorAngle;
        tailRotor.render(scale * 1.3f);
        // rotate 1/4 turn and render again
        tailRotor.rotateAngleZ += 1.5707f;
        tailRotor.render(scale * 1.3f);
        
        windshield.render(scale * 1.3f);
    }
}
