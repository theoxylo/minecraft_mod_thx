package net.minecraft.src;

public class ThxModelHelicopter extends ThxModel
{
    boolean bottomVisible = true;
    
    float rotorSpeed = 0f;
    float lastRotorRad = 0f;
    float lastTailRotorRad = 0f;
    float MAX_ROTOR_SPEED = 18f * ((float)ThxConfig.getIntProperty("rotor_speed_percent")) / 100f;
    
    float SPIN_UP_TIME = 10f;
    float timeSpun = 0f;
    
    public ThxModelHelicopter()
    {
        ENABLE_ROTOR = ThxConfig.getBoolProperty("enable_rotor");
        
        // dimensions -- adjusted and reused when adding each model box
        float length = 0f;
        float width  = 0f;
        float height = 0f;
        
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
        
        // bottom, make invisible for looking down
        boxes[0].addBox(-byte0 / 2, -byte2 / 2 + 2, -3F, byte0, byte2 - 4, 4);
        boxes[0].setRotationPoint(0.0F, 0 + byte3, 0.0F);
        boxes[0].rotateAngleX = 1.570796F;
        
        boxes[1].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2);
        boxes[1].setRotationPoint(-byte0 / 2 + 1, 0 + byte3, 0.0F);
        boxes[1].rotateAngleY = 4.712389F;
        
        boxes[2].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2);
        boxes[2].setRotationPoint(byte0 / 2 - 1, 0 + byte3, 0.0F);
        boxes[2].rotateAngleY = 1.570796F;
        
        boxes[3].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2);
        boxes[3].setRotationPoint(0.0F, 0 + byte3, -byte2 / 2 + 1);
        boxes[3].rotateAngleY = 3.141593F;
        
        boxes[4].addBox(-byte0 / 2 + 2, -byte1 - 1, -1F, byte0 - 4, byte1, 2);
        boxes[4].setRotationPoint(0.0F, 0 + byte3, byte2 / 2 - 1);

        // new body, large enough to hide player model in 3rd person view.
        // in 1st person view, it is invisible
        //length = 28f;
        //width  = 20f;
        //height = 24f;
        //body = new ModelRenderer(0, 0);
        //body.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        //body.setRotationPoint(0f, -9f, 0f);
        
        // rotor
        length = 2f;
        width = 64f;
        height = 1f;
        rotor1 = new ModelRenderer(0, 0);
        rotor1.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        rotor1.setRotationPoint(6f, -24f, 0f);
        rotor1.rotateAngleY = 1.75f; // start off axis for realism
        
        // tail rotor
        length = 16f;
        width  = 1f;
        height = 2f;
        tailRotor = new ModelRenderer(0, 0);
        tailRotor.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        //tailRotor.setRotationPoint(28f, -22f, 3f);
        // good: tailRotor.setRotationPoint(34, -14f, 3f);
        tailRotor.setRotationPoint(36f, -14f, 3f);

        // tail
        length = 22f;
        width = 4f;
        height = 4f;
        tail = new ModelRenderer(0, 0);
        tail.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        tail.setRotationPoint(27, -14, 0f);

        // rotor vertical support
        length = 4f;
        width = 4f;
        height = 24f;
        rotor2 = new ModelRenderer(0, 0);
        rotor2.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        rotor2.setRotationPoint(14f, -9f, 0f);

        // rotor horizontal support
        length = 12;
        width = 4f;
        height = 2f;
        rotor3 = new ModelRenderer(0, 0);
        rotor3.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        rotor3.setRotationPoint(10, -22, 0f);

        // cockpit1 vertical support
        length = 1f;
        width = 1f;
        height = 13f;
        cockpit1 = new ModelRenderer(0, 0);
        cockpit1.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        cockpit1.setRotationPoint(-10f, -9f, 9f);
        
        // cockpit2 vertical support
        length = 1f;
        width = 1f;
        height = 13f;
        cockpit2 = new ModelRenderer(0, 0);
        cockpit2.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        cockpit2.setRotationPoint(-10f, -9f, -9f);
        
        // cockpit3 horizontal support
        length = 1f;
        width = 19f;
        height = 1f;
        cockpit3 = new ModelRenderer(0, 0);
        cockpit3.addBox(-length / 2f, -height / 2f, -width / 2f, (int) length, (int) height, (int) width);
        cockpit3.setRotationPoint(-10f, -16f, 0f);
    }

    public void render()
    {
        update();
        
        float f5 = 0.0625f;
        
        //System.out.println("Model delta time sec: " + deltaTime);
        
        if (!visible) return;
        
        for(int i = 0; i < 5; i++)
        {
            if (i == 0 && !bottomVisible)
            {
                continue; // skip bottom for looking down
            }
            boxes[i].render(f5);
        }
        
        if (ENABLE_ROTOR)
        {
            if (rotorSpeed > 0f)
            {
                if (timeSpun < SPIN_UP_TIME)
                {
                    timeSpun += deltaTime * 3f; // spin up faster than spin down
                    
                    rotor1.rotateAngleY    += deltaTime * MAX_ROTOR_SPEED * rotorSpeed * timeSpun / SPIN_UP_TIME;
			        tailRotor.rotateAngleZ -= deltaTime * MAX_ROTOR_SPEED * timeSpun / SPIN_UP_TIME; // not linked to throttle
                }
                else
                {
                    rotor1.rotateAngleY    += deltaTime * MAX_ROTOR_SPEED * rotorSpeed;
                    tailRotor.rotateAngleZ -= deltaTime * MAX_ROTOR_SPEED;
                }
                
		        if (rotor1.rotateAngleY > 2*PI) rotor1.rotateAngleY -= 2*PI;
                rotor1.render(f5);
                rotor1.rotateAngleY += 1.5707f; // add second blade perp
                rotor1.render(f5);

		        if (tailRotor.rotateAngleZ < 2*PI) tailRotor.rotateAngleZ += 2*PI;
		        tailRotor.render(f5);
                tailRotor.rotateAngleZ -= 1.5707f; // add second blade perp
                tailRotor.render(f5);
            }
            else
            {
                rotorSpeed = 0f;
                
                if (timeSpun > 0f)
                {
                    timeSpun -= deltaTime;
                    
                    rotor1.rotateAngleY += deltaTime * MAX_ROTOR_SPEED * (1 - MathHelper.cos(timeSpun / SPIN_UP_TIME));
                    tailRotor.rotateAngleZ += deltaTime * MAX_ROTOR_SPEED * (1 - MathHelper.cos(timeSpun / SPIN_UP_TIME));
                    
                    // remember stopping position
	                lastRotorRad = rotor1.rotateAngleY;
	                lastTailRotorRad = tailRotor.rotateAngleZ;
                }
                else
                {
                    rotor1.rotateAngleY = lastRotorRad;
                    tailRotor.rotateAngleZ = lastTailRotorRad;
                }
                
                rotor1.render(f5);
                rotor1.rotateAngleY += 1.5707f; // add second blade perp
                rotor1.render(f5);
                
		        tailRotor.render(f5);
                tailRotor.rotateAngleZ -= 1.5707f; // add second blade perp
                tailRotor.render(f5);
            }
        }
        else
        {
            // show fixed rotor by rendering twice
            rotor1.rotateAngleY = 0.7854f;
            rotor1.render(f5);
            rotor1.rotateAngleY += 1.5707f;
            rotor1.render(f5);
            
            tailRotor.rotateAngleZ = 0.7854f;
            tailRotor.render(f5);
            tailRotor.rotateAngleZ += 1.5707f;
            tailRotor.render(f5);
        }
        
        tail.render(f5);
        rotor2.render(f5);
        rotor3.render(f5);
        
        cockpit1.render(f5);
        cockpit2.render(f5);
        cockpit3.render(f5);
        
        //body.render(f5);
    }

    boolean ENABLE_ROTOR;
    public ModelRenderer rotor1;
    public ModelRenderer rotor2;
    public ModelRenderer rotor3;
    
    public ModelRenderer cockpit1;
    public ModelRenderer cockpit2;
    public ModelRenderer cockpit3;

    public ModelRenderer boxes[];
    
    public ModelRenderer tail;
    public ModelRenderer tailRotor;
    
    //public ModelRenderer body;
}
