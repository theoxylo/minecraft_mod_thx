package net.minecraft.src;

public class ThxModelHelicopter extends ThxModel
{
    int rotorOn = 1;
    float lastRotorRad = 0f;
    //float MAX_ROTOR_SPEED = .30f; //.66f;
    float MAX_ROTOR_SPEED = ((float)ThxConfig.getIntProperty("rotor_speed_percent")) / 100f;
    
    float SPIN_UP_TIME = 300f;
    float timeSpun = 0f;
    
    public ThxModelHelicopter()
    {
	    yawOffset = 90f;

        ENABLE_ROTOR = ThxConfig.getBoolProperty("enable_rotor");
        
        // dimensions -- adjusted and reused when adding each model box
        float length = 0f;
        float width  = 0f;
        float height = 0f;
        
        float xOff = 4f;

        boxes = new ModelRenderer[5];
        boxes[0] = new ModelRenderer(0, 8);
        boxes[1] = new ModelRenderer(0, 0);
        boxes[2] = new ModelRenderer(0, 0);
        boxes[3] = new ModelRenderer(0, 0);
        boxes[4] = new ModelRenderer(0, 0);
        
        byte byte0 = 24;
        byte byte1 = 6;
        byte byte2 = 20;
        byte byte3 = 4;
        
        // body, based on original mc boat
        boxes[0].addBox(-byte0 / 2, -byte2 / 2 + 2, -3F, byte0, byte2 - 4, 4);
        boxes[0].setPosition(0.0F, 0 + byte3, 0.0F);
        boxes[0].rotateAngleX = 1.570796F;
        
        boxes[1].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2);
        boxes[1].setPosition(-byte0 / 2 + 1, 0 + byte3, 0.0F);
        boxes[1].rotateAngleY = 4.712389F;
        
        boxes[2].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2);
        boxes[2].setPosition(byte0 / 2 - 1, 0 + byte3, 0.0F);
        boxes[2].rotateAngleY = 1.570796F;
        
        boxes[3].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2);
        boxes[3].setPosition(0.0F, 0 + byte3, -byte2 / 2 + 1);
        boxes[3].rotateAngleY = 3.141593F;
        
        boxes[4].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2);
        boxes[4].setPosition(0.0F, 0 + byte3, byte2 / 2 - 1);

        // rotor
        length = 2f;
        width = 64f;
        height = 1f;
        rotor1 = new ModelRenderer(0, 0);
        rotor1.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        rotor1.setPosition(0f, -24f, 0f);
        rotor1.rotateAngleY = 1.75f; // start off axis for realism

        // rotor vertical support
        length = 4f;
        width = 4f;
        height = 24f;
        rotor2 = new ModelRenderer(0, 0);
        rotor2.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        rotor2.setPosition(14f, -9f, 0f);

        // rotor horizontal support
        length = 32f;
        width = 4f;
        height = 2f;
        rotor3 = new ModelRenderer(0, 0);
        rotor3.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        rotor3.setPosition(14f, -22, 0f);

        // cockpit1 vertical support
        length = 1f;
        width = 1f;
        height = 13f;
        cockpit1 = new ModelRenderer(0, 0);
        cockpit1.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        cockpit1.setPosition(-10f, -9f, 9f);
        
        // cockpit2 vertical support
        length = 1f;
        width = 1f;
        height = 13f;
        cockpit2 = new ModelRenderer(0, 0);
        cockpit2.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        cockpit2.setPosition(-10f, -9f, -9f);
        
        // cockpit3 horizontal support
        length = 1f;
        width = 19f;
        height = 1f;
        cockpit3 = new ModelRenderer(0, 0);
        cockpit3.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        cockpit3.setPosition(-10f, -16f, 0f);
    }

    @Override
    public void render(float f, float f1, float f2, float f3, float f4, float f5)
    {
        for(int i = 0; i < 5; i++)
        {
            boxes[i].render(f5);
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
            // show fixed rotor by rendering twice
            rotor1.rotateAngleY = 0.7854f;
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

    public ModelRenderer boxes[];
}
