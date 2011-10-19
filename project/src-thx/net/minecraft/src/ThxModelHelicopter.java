package net.minecraft.src;

public class ThxModelHelicopter extends ThxModel
{
    boolean bottomVisible = true;
    float scale = 0.0625f;
    
    float rotorSpeed = 0f;
    float lastRotorRad = 0f;
    float lastTailRotorRad = 0f;
    float MAX_ROTOR_SPEED = 18f * ((float)ThxConfig.getIntProperty("rotor_speed_percent")) / 100f;
    
    float SPIN_UP_TIME = 10f;
    float timeSpun = 0f;

    boolean ENABLE_ROTOR;
    public ModelRenderer mainRotor;
    public ModelRenderer rotor2;
    public ModelRenderer rotor3;
    
    public ModelRenderer cockpit1;
    public ModelRenderer cockpit2;
    public ModelRenderer cockpit3;

    public ModelRenderer bottom;
    public ModelRenderer frontWall;
    public ModelRenderer backWall;
    public ModelRenderer leftWall;
    public ModelRenderer rightWall;
    
    public ModelRenderer tail;
    public ModelRenderer tailRotor;
    
    //public ModelRenderer body;
    
    public ThxModelHelicopter()
    {
        renderTexture = "/thx/helicopter.png";

        ENABLE_ROTOR = ThxConfig.getBoolProperty("enable_rotor");
        
        // dimensions -- adjusted and reused when adding each model box
        float length = 0f;
        float height = 0f;
        float width  = 0f;

        bottom:
        {
	        // 1. to save texture space, we minimize width (Z value) by rotating. so 20x4x16 -> 20x16x4
	        // 2. then we have to rotate piece back into place. rotate about x axis (roll) to swap y and z
	        // 3. make half size and then scale later to save texture area (but half rez too) -> 10x8x2  
	        // 4. also have to adjust ("reduce") position (rotationPt) since it moves when we scale
	        // 5. but at least rotationPt is not affected by rotation (by definition), so no adjustment for that
	        length = 10f;
	        height = 8f;
	        width = 2f;
	        bottom = new ModelRenderer(this, 40, 22); // texture offset: 
	        bottom.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        bottom.rotationPointY = 2f; // halved to adjust for scale 	        bottom.rotateAngleX = PI / 2f; // 90 deg roll left to lay flat
        }
        
        frontWall:
        {
            // rotate about Y, so 2x6x20 -> 20x6x2
	        length = 20f;
	        height = 6f;
	        width = 2f;
	        frontWall = new ModelRenderer(this, 20, 14);
	        frontWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        frontWall.setRotationPoint(-11f, 0f, 0f); // placed 11 unit in front
	        frontWall.rotateAngleY = PI * 1.5f; // 270 deg yaw
        }
        
        backWall:
        {
	        length = 20f;
	        height = 6f;
	        width = 2f;
	        backWall = new ModelRenderer(this, 20, 14);
	        backWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        backWall.setRotationPoint(11, 0f, 0f);
	        backWall.rotateAngleY = PI / 2f; // 90 deg yaw
        }
        
        leftWall:
        {
	        length = 20f;
	        height = 6f;
	        width = 2f;
	        leftWall = new ModelRenderer(this, 20, 6);
	        leftWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        leftWall.setRotationPoint(0f, 0f, -9f);
        }
        
        rightWall:
        {
	        length = 20f;
	        height = 6f;
	        width = 2f;
	        rightWall = new ModelRenderer(this, 20, 6);
	        rightWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        rightWall.setRotationPoint(0f, 0f, 9f);
	        rightWall.rotateAngleY = PI; // flip 180 deg so decal is on outside
        }
        
        mainRotor: //will be scaled x2
        {
	        length = 30f;
	        height =  0f;
	        width  =  1f;
	        mainRotor = new ModelRenderer(this, 0, 0);
	        mainRotor.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        //mainRotor.setRotationPoint(6f, -24f, 0f);
	        mainRotor.setRotationPoint(3f, -11.7f, 0f); // halved to adjust for scale, and a little subtracted 
	        
	        mainRotor.rotateAngleY = 1.75f; // start off axis for realism
        }
        
        tailRotor:
        {
	        length = 16f;
	        height = 2f;
	        width  = 0f;
	        tailRotor = new ModelRenderer(this, 8, 30);
	        tailRotor.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        tailRotor.setRotationPoint(34f, -14f, 1.4f);
        }

        tail: // will be scaled x2
        {
	        length = 10f;
	        height =  2f;
	        width  =  1f;
	        tail = new ModelRenderer(this, 12, 2);
	        tail.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        //tail.setRotationPoint(26, -14, 0f);
	        tail.setRotationPoint(13f, -7f, 0f); // adjusted for scale
        }

        rotorVerticalSupport:
        {
	        length = 4f;
	        height = 22f;
	        width = 3f;
	        rotor2 = new ModelRenderer(this, 0, 3);
	        rotor2.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        rotor2.setRotationPoint(14f, -10f, 0f);
        }

        rotorHorizontalSupport:
        {
	        length = 12;
	        height = 2f;
	        width = 3f;
	        rotor3 = new ModelRenderer(this, 34, 1);
	        rotor3.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        rotor3.setRotationPoint(10, -22, 0f);
        }

        // cockpit1 vertical support
        cockpit1:
        {
	        length = 1f;
	        height = 13f;
	        width = 1f;
	        cockpit1 = new ModelRenderer(this, 15, 7);
	        cockpit1.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        cockpit1.setRotationPoint(-10f, -9f, 9f);
        }
        
        // cockpit2 vertical support
        cockpit2:
        {
	        length = 1f;
	        height = 13f;
	        width = 1f;
	        cockpit2 = new ModelRenderer(this, 15, 7);
	        cockpit2.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        cockpit2.setRotationPoint(-10f, -9f, -9f);
        }
        
        // cockpit3 horizontal support
        cockpit3:
        {
	        length =  1f;
	        height = 19f;
	        width  =  1f;
	        cockpit3 = new ModelRenderer(this, 15, 7);
	        cockpit3.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
	        cockpit3.setRotationPoint(-10f, -16f, 0f);
	        cockpit3.rotateAngleX = PI / 2f;
        }
    }

    public void render()
    {
        update();
        
        //System.out.println("Model delta time sec: " + deltaTime);
        
        if (!visible) return;
        
        if (bottomVisible) bottom.render(scale * 2f);
        
        frontWall.render(scale);
        backWall.render(scale);
        leftWall.render(scale);
        rightWall.render(scale);
        
        // windshield
        cockpit1.render(scale);
        cockpit2.render(scale);
        cockpit3.render(scale);
        
        // rotor supports
        rotor2.render(scale);
        rotor3.render(scale);
        
        tail.render(scale * 2f);
        
        if (ENABLE_ROTOR)
        {
            if (rotorSpeed > 0f)
            {
                if (timeSpun < SPIN_UP_TIME)
                {
                    timeSpun += deltaTime * 3f; // spin up faster than spin down
                    
                    mainRotor.rotateAngleY += deltaTime * MAX_ROTOR_SPEED * rotorSpeed * timeSpun / SPIN_UP_TIME;
			        tailRotor.rotateAngleZ -= deltaTime * MAX_ROTOR_SPEED * timeSpun / SPIN_UP_TIME; // not linked to throttle
                }
                else
                {
                    mainRotor.rotateAngleY += deltaTime * MAX_ROTOR_SPEED * rotorSpeed;
                    tailRotor.rotateAngleZ -= deltaTime * MAX_ROTOR_SPEED;
                }
                
		        if (mainRotor.rotateAngleY > 2*PI) mainRotor.rotateAngleY -= 2*PI;
                mainRotor.render(scale * 2f);
                mainRotor.rotateAngleY += 1.5707f; // add second blade perp
                mainRotor.render(scale * 2f);

		        if (tailRotor.rotateAngleZ < 2*PI) tailRotor.rotateAngleZ += 2*PI;
		        tailRotor.render(scale);
                tailRotor.rotateAngleZ -= 1.5707f; // add second blade perp
                tailRotor.render(scale);
            }
            else
            {
                rotorSpeed = 0f;
                
                if (timeSpun > 0f)
                {
                    timeSpun -= deltaTime;
                    
                    mainRotor.rotateAngleY += deltaTime * MAX_ROTOR_SPEED * (1 - MathHelper.cos(timeSpun / SPIN_UP_TIME));
                    tailRotor.rotateAngleZ += deltaTime * MAX_ROTOR_SPEED * (1 - MathHelper.cos(timeSpun / SPIN_UP_TIME));
                    
                    // remember stopping position
	                lastRotorRad = mainRotor.rotateAngleY;
	                lastTailRotorRad = tailRotor.rotateAngleZ;
                }
                else
                {
                    mainRotor.rotateAngleY = lastRotorRad;
                    tailRotor.rotateAngleZ = lastTailRotorRad;
                }
                
                mainRotor.render(scale * 2f);
                mainRotor.rotateAngleY += 1.5707f; // add second blade perp
                mainRotor.render(scale * 2f);
                
		        tailRotor.render(scale);
                tailRotor.rotateAngleZ -= 1.5707f; // add second blade perp
                tailRotor.render(scale);
            }
        }
        else
        {
            // show fixed rotor by rendering twice
            mainRotor.rotateAngleY = 0.7854f;
            mainRotor.render(scale * 2f);
            mainRotor.rotateAngleY += 1.5707f;
            mainRotor.render(scale * 2f);
            
            tailRotor.rotateAngleZ = 0.7854f;
            tailRotor.render(scale);
            tailRotor.rotateAngleZ += 1.5707f;
            tailRotor.render(scale);
        }
    }
}
