package net.minecraft.src;

public class ThxModelHelicopter extends ModelBase
{
    int rotorOn = 1;
    float lastRotorRad = 0f;
    //float MAX_ROTOR_SPEED = .30f; //.66f;
    float MAX_ROTOR_SPEED = ((float)ThxConfig.getIntProperty("rotor_speed_percent")) / 100f;
    
    float SPIN_UP_TIME = 300f;
    float timeSpun = 0f;

    /*
     * public ModelRenderer box;
     * 
     * int textureOffsetX = 0; int textureOffsetY = 0;
     * 
     * public ThxModelHelicopter() { float length = 56f; float width = 24f;
     * float height = 32f; box = new ModelRenderer(textureOffsetX,
     * textureOffsetY); box.addBox(-length/2f, -height/2f, -width/2f,
     * (int)length, (int)height, (int)width, 0.0f); box.setPosition(0f, 0f, 0f);
     * //box.rotateAngleX = 1.570796F; }
     * 
     * @Override public void render(float f, float f1, float f2, float f3, float
     * f4, float f5) { box.render(f5); }
     */

    public ThxModelHelicopter()
    {
        ENABLE_ROTOR = ThxConfig.getBoolProperty("enable_rotor");
        
        float scale = 0f;
        float xOff = 4f;

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
        
        boatSides[0].addBox(-byte0 / 2, -byte2 / 2 + 2, -3F, byte0, byte2 - 4, 4, scale);
        boatSides[0].setPosition(0.0F, 0 + byte3, 0.0F);
        //boatSides[0].addBox(-24f / 2f, -20f / 2f + 2, -3F, 24, 20 - 4, 8, 0.0F);
        //boatSides[0].setPosition(0f, 8f, 0f);
        boatSides[0].rotateAngleX = 1.570796F;
        
        boatSides[1].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2, scale);
        boatSides[1].setPosition(-byte0 / 2 + 1, 0 + byte3, 0.0F);
        boatSides[1].rotateAngleY = 4.712389F;
        
        boatSides[2].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2, scale);
        boatSides[2].setPosition(byte0 / 2 - 1, 0 + byte3, 0.0F);
        boatSides[2].rotateAngleY = 1.570796F;
        
        boatSides[3].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2, scale);
        boatSides[3].setPosition(0.0F, 0 + byte3, -byte2 / 2 + 1);
        boatSides[3].rotateAngleY = 3.141593F;
        
        boatSides[4].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2, scale);
        boatSides[4].setPosition(0.0F, 0 + byte3, byte2 / 2 - 1);

        // rotor
        float length = 2f;
        float width = 64f;
        float height = 1f;
        rotor1 = new ModelRenderer(0, 0);
        rotor1.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width, 0.0f);
        rotor1.setPosition(0f, -24f, 0f);
        rotor1.rotateAngleY = 1.75f; // start off axis for realism

        // rotor vertical support
        length = 4f;
        width = 4f;
        height = 24f;
        rotor2 = new ModelRenderer(0, 0);
        rotor2.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width, 0.0f);
        rotor2.setPosition(14f, -9f, 0f);

        // rotor horizontal support
        length = 32f;
        width = 4f;
        height = 2f;
        rotor3 = new ModelRenderer(0, 0);
        rotor3.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width, 0.0f);
        rotor3.setPosition(14f, -22, 0f);

        // cockpit1 vertical support
        length = 1f;
        width = 1f;
        height = 13f;
        cockpit1 = new ModelRenderer(0, 0);
        cockpit1.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width, 0.0f);
        cockpit1.setPosition(-10f, -9f, 9f);
        
        // cockpit2 vertical support
        length = 1f;
        width = 1f;
        height = 13f;
        cockpit2 = new ModelRenderer(0, 0);
        cockpit2.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width, 0.0f);
        cockpit2.setPosition(-10f, -9f, -9f);
        
        // cockpit3 horizontal support
        length = 1f;
        width = 19f;
        height = 1f;
        cockpit3 = new ModelRenderer(0, 0);
        cockpit3.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width, 0.0f);
        cockpit3.setPosition(-10f, -16f, 0f);
    }

    @Override
    public void render(float f, float f1, float f2, float f3, float f4, float f5)
    {
        for(int i = 0; i < 5; i++)
        {
            boatSides[i].render(f5);
        }
        
        if (ENABLE_ROTOR)
        {
            if (rotorOn > 0)
            {
                if (timeSpun < SPIN_UP_TIME)
                {
                    timeSpun += 1f;
                    rotor1.rotateAngleY += MAX_ROTOR_SPEED * timeSpun / SPIN_UP_TIME;
                }
                else rotor1.rotateAngleY += MAX_ROTOR_SPEED;
                
                rotor1.render(f5);
                rotor1.rotateAngleY += 1.5707f; // add second blade perp
                rotor1.render(f5);

            }
            else
            {
                if (timeSpun > 0f)
                {
                    timeSpun -= 1f;
                    rotor1.rotateAngleY += MAX_ROTOR_SPEED * (1 - MathHelper.cos(timeSpun / SPIN_UP_TIME));
	                lastRotorRad = rotor1.rotateAngleY;
                }
                else rotor1.rotateAngleY = lastRotorRad;
                
                rotor1.render(f5);
                rotor1.rotateAngleY += 1.5707f; // add second blade perp
                rotor1.render(f5);
            }
        }
        else
        {
            // show fixed x rotor by rendering twice
            rotor1.rotateAngleY = .9f;
            rotor1.render(f5);
            rotor1.rotateAngleY += 1.5707f;
            rotor1.render(f5);
        }
        
        rotor2.render(f5);
        rotor3.render(f5);
        
        cockpit1.render(f5);
        cockpit2.render(f5);
        cockpit3.render(f5);

    }

    boolean ENABLE_ROTOR;
    public ModelRenderer rotor1;
    public ModelRenderer rotor2;
    public ModelRenderer rotor3;
    
    public ModelRenderer cockpit1;
    public ModelRenderer cockpit2;
    public ModelRenderer cockpit3;

    public ModelRenderer boatSides[];
}
