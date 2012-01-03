package net.minecraft.src;

public class ThxModelHelicopter extends ThxModel
{
    boolean bottomVisible = true;

    float scale = 0.0625f;
    float x2scale = 0.125f;
    float centerScale = 1.0f;
    
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

    public ModelRenderer windshield;
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
        
        // All model boxes are now scaled x2 at rendering time to save texture space
        // (with the exception of the windsheield which is already min size)

        bottom:
        {
            // 1. to save texture space, create piece in upright orientation
            // 2. then we have to rotate piece back into place
            // 3. make half size and then scale later to save texture area  
            // 4. also have to adjust ("reduce") position (rotationPt) since it moves when we scale
            float length = 10f;
            float height = 8f;
            float width  = 2f;
            bottom = new ModelRenderer(this, 0, 22); // texture offset: 
            bottom.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            bottom.setRotationPoint(0f * centerScale, 2f * centerScale, 0f * centerScale); // halved to adjust for scale            bottom.rotateAngleX = PI / 2f; // 90 deg roll left to lay flat
        }
        frontWall:
        {
            // rotate about Y, so 2x6x20 -> 20x6x2
            // will be scaled x2 at render
            float length = 10f;
            float height =  3f;
            float width  =  1f;
            frontWall = new ModelRenderer(this, 0, 4);
            frontWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            frontWall.setRotationPoint(-5.5f * centerScale, 0f * centerScale, 0f * centerScale); // placed 11 unit in front
            frontWall.rotateAngleY = PI * 1.5f; // 270 deg yaw
        }
        backWall:
        {
            // will be scaled x2 at render
            float length = 10f;
            float height =  3f;
            float width  =  1f;
            backWall = new ModelRenderer(this, 0, 9);
            backWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            backWall.setRotationPoint(5.5f * centerScale, 0f * centerScale, 0f * centerScale);
            backWall.rotateAngleY = PI / 2f; // 90 deg yaw
        }
        leftWall:
        {
            // will be scaled x2 at render
            float length = 10f;
            float height =  3f;
            float width  =  1f;
            leftWall = new ModelRenderer(this, 25, 19);
            leftWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            leftWall.setRotationPoint(0f * centerScale, 0f * centerScale, -4.5f * centerScale);
        }
        rightWall:
        {
            // will be scaled x2 at render
            float length = 10f;
            float height =  3f;
            float width  =  1f;
            rightWall = new ModelRenderer(this, 25, 24);
            rightWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            rightWall.setRotationPoint(0f * centerScale, 0f * centerScale, 4.5f * centerScale);
            rightWall.rotateAngleY = PI; // flip 180 deg so decal is on outside
        }
        mainRotor: 
        {
            // will be scaled x2 at render
            float length = 30f;
            float height =  0f;
            float width  =  1f;
            mainRotor = new ModelRenderer(this, 0, 0);
            mainRotor.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            mainRotor.setRotationPoint(2f * centerScale, -11.7f * centerScale, 0f * centerScale); // halved to adjust for scale, and a little subtracted 
        }
        tailRotor:
        {
            // will be scaled x2 at render
            float length = 8f;
            float height = 1f;
            float width  = 0f;
            tailRotor = new ModelRenderer(this, 0, 2);
            tailRotor.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            tailRotor.setRotationPoint(16f * centerScale, -7f * centerScale, .7f * centerScale);
        }
        tail:
        {
            // will be scaled x2 at render
            float length = 10f;
            float height =  2f;
            float width  =  1f;
            tail = new ModelRenderer(this, 42, 29);
            tail.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            tail.setRotationPoint(12f * centerScale, -7f * centerScale, 0f * centerScale);
        }
        rotorVerticalSupport:
        {
            // will be scaled x2 at render
            float length =  1f;
            float height = 11f;
            float width  =  2f;
            rotor2 = new ModelRenderer(this, 58, 11);
            rotor2.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            rotor2.setRotationPoint(6.5f * centerScale, -5f * centerScale, 0f * centerScale);
        }
        rotorHorizontalSupport:
        {
            // will be scaled x2 at render
            float length =  6f;
            float height =  1f;
            float width  =  2f;
            rotor3 = new ModelRenderer(this, 48, 25);
            rotor3.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            rotor3.setRotationPoint(4f * centerScale, -11f * centerScale, 0f * centerScale);
        }
        cockpit1:
        {
            // cockpit1 right vertical support
            float length =  1f;
            float height = 13f;
            float width  =  1f;
            cockpit1 = new ModelRenderer(this, 43, 4);
            cockpit1.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            cockpit1.setRotationPoint(-11f * centerScale, -9f * centerScale, 9f * centerScale);
            cockpit1.rotateAngleY = PI / 2f;
        }
        cockpit2:
        {
            // cockpit2 left vertical support
            float length =  1f;
            float height = 13f;
            float width  =  1f;
            cockpit2 = new ModelRenderer(this, 48, 4);
            cockpit2.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            cockpit2.setRotationPoint(-11f * centerScale, -9f * centerScale, -9f * centerScale);
        }
        cockpit3:
        {
            // cockpit3 horizontal support
            float length =  1f;
            float height = 19f;
            float width  =  1f;
            cockpit3 = new ModelRenderer(this, 53, 4);
            cockpit3.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            cockpit3.setRotationPoint(-11f * centerScale, -16f * centerScale, 0f * centerScale);
            cockpit3.rotateAngleX = PI / 2f;
            cockpit3.rotateAngleZ = PI / 2f;
        }
        windshield:
        {
            float length = 9f;
            float height = 7f;
            float width  = 0f;
            windshield = new ModelRenderer(this, 22, 2);
            windshield.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            windshield.setRotationPoint(-5.5f * centerScale, -4.5f * centerScale, 0f * centerScale);
            windshield.rotateAngleY = PI * 1.5f; // 270 deg yaw
        }
    }

    public void render()
    {
        update();
        
        //System.out.println("Model delta time sec: " + deltaTime);
        
        if (!visible) return;
        
        if (bottomVisible) bottom.render(x2scale);

        frontWall.render(x2scale);
        backWall.render(x2scale);
        leftWall.render(x2scale);
        rightWall.render(x2scale);
        
        // windshield
        cockpit1.render(scale);
        cockpit2.render(scale);
        cockpit3.render(scale);
        windshield.render(x2scale);
        
        // rotor supports
        rotor2.render(x2scale);
        rotor3.render(x2scale);
        
        tail.render(x2scale);
        
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
                mainRotor.render(x2scale);
                mainRotor.rotateAngleY += 1.5707f; // add second blade perp
                mainRotor.render(x2scale);

                if (tailRotor.rotateAngleZ < 2*PI) tailRotor.rotateAngleZ += 2*PI;
                tailRotor.render(x2scale);
                tailRotor.rotateAngleZ -= 1.5707f; // add second blade perp
                tailRotor.render(x2scale);
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
                
                mainRotor.render(x2scale);
                mainRotor.rotateAngleY += 1.5707f; // add second blade perp
                mainRotor.render(x2scale);
                
                tailRotor.render(x2scale);
                tailRotor.rotateAngleZ -= 1.5707f; // add second blade perp
                tailRotor.render(x2scale);
            }
        }
        else
        {
            // show fixed rotor by rendering twice
            mainRotor.rotateAngleY = 0.7854f;
            mainRotor.render(x2scale);
            mainRotor.rotateAngleY += 1.5707f;
            mainRotor.render(x2scale);
            
            tailRotor.rotateAngleZ = 0.7854f;
            tailRotor.render(x2scale);
            tailRotor.rotateAngleZ += 1.5707f;
            tailRotor.render(x2scale);
        }
    }
}
