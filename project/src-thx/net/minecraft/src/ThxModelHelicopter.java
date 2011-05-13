package net.minecraft.src;

public class ThxModelHelicopter extends ModelBase
{
    /*
    public ModelRenderer box;
    
    int textureOffsetX = 0;
    int textureOffsetY = 0;
    
    public ThxModelHelicopter()
    {
        float length = 56f;
        float width = 24f;
        float height = 32f;
        box = new ModelRenderer(textureOffsetX, textureOffsetY);
        box.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width, 0.0f);
        box.setPosition(0f, 0f, 0f);
        //box.rotateAngleX = 1.570796F;
    }

    @Override
    public void render(float f, float f1, float f2, float f3, float f4, float f5)
    {
        box.render(f5);
    }
    */

    public ThxModelHelicopter()
    {
        ENABLE_ROTOR = ThxConfig.getBoolProperty("enable_rotor");

        boatSides = new ModelRenderer[5];
        boatSides[0] = new ModelRenderer(0, 8);
        boatSides[1] = new ModelRenderer(0, 0);
        boatSides[2] = new ModelRenderer(0, 0);
        boatSides[3] = new ModelRenderer(0, 0);
        boatSides[4] = new ModelRenderer(0, 0);
        byte byte0 = 24;
        byte byte1 = 6;
        byte byte2 = 20;
        byte byte3 = 4;
        boatSides[0].addBox(-byte0 / 2, -byte2 / 2 + 2, -3F, byte0, byte2 - 4, 4, 0.0F);
        boatSides[0].setPosition(0.0F, 0 + byte3, 0.0F);
        boatSides[1].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2, 0.0F);
        boatSides[1].setPosition(-byte0 / 2 + 1, 0 + byte3, 0.0F);
        boatSides[2].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2, 0.0F);
        boatSides[2].setPosition(byte0 / 2 - 1, 0 + byte3, 0.0F);
        boatSides[3].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2, 0.0F);
        boatSides[3].setPosition(0.0F, 0 + byte3, -byte2 / 2 + 1);
        boatSides[4].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2, 0.0F);
        boatSides[4].setPosition(0.0F, 0 + byte3, byte2 / 2 - 1);
        boatSides[0].rotateAngleX = 1.570796F;
        boatSides[1].rotateAngleY = 4.712389F;
        boatSides[2].rotateAngleY = 1.570796F;
        boatSides[3].rotateAngleY = 3.141593F;
        
        // rotor
        float length = 2f;
        float width = 64f;
        float height = 1f;
        rotor1 = new ModelRenderer(0, 0);
        rotor1.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width, 0.0f);
        rotor1.setPosition(0f, -24f, 0f);
        
        // rotor vertical support
        length = 4f;
        width = 4f;
        height = 24f;
        rotor2 = new ModelRenderer(0, 0);
        rotor2.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width, 0.0f);
        rotor2.setPosition(14f, -11f, 0f);

        // rotor horizontal support
        length = 32f;
        width = 4f;
        height = 2f;
        rotor3 = new ModelRenderer(0, 0);
        rotor3.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width, 0.0f);
        rotor3.setPosition(14f, -22, 0f);

    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5)
    {
        for(int i = 0; i < 5; i++)
        {
            boatSides[i].render(f5);
        }
        
        if (ENABLE_ROTOR)
        {
	        rotor1.rotateAngleY += .66;
	        rotor1.render(f5);
        }
        
        rotor2.render(f5);
        rotor3.render(f5);

    }

    boolean ENABLE_ROTOR;
    public ModelRenderer rotor1;
    public ModelRenderer rotor2;
    public ModelRenderer rotor3;

    public ModelRenderer boatSides[];
}
