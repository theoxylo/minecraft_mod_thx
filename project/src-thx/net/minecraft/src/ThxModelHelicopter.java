package net.minecraft.src;

public class ThxModelHelicopter extends ThxModel
{
    boolean bottomVisible = true;

    float scale = 0.0625f;
    float x2scale = 0.125f;
    
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
        
        // All model boxes are now scaled x2 at rendering time to save texture space
        // (with the exception of the windsheield which is already min size)

        bottom:
        {
            // 1. to save texture space, create piece in upright orientation. so 20x4x16 -> 16x20x4
            // 2. then we have to rotate piece back into place. rotate about y, then x axis
            // 3. make half size and then scale later to save texture area (but half rez too) -> 8x10x2  
            // 4. also have to adjust ("reduce") position (rotationPt) since it moves when we scale
            float length =  8f;
            float height = 10f;
            float width  =  2f;
            bottom = new ModelRenderer(this, 0, 20); // texture offset: 
            bottom.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            bottom.rotationPointY = 2f; // halved to adjust for scale             bottom.rotateAngleY = PI / 2f; // 90 deg roll left to lay flat
            bottom.rotateAngleX = PI / 2f; // 90 deg roll left to lay flat
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
            frontWall.setRotationPoint(-5.5f, 0f, 0f); // placed 11 unit in front
            frontWall.rotateAngleY = PI * 1.5f; // 270 deg yaw
        }
        backWall:
        {
            // will be scaled x2 at render
            float length = 10f;
            float height =  3f;
            float width  =  1f;
            backWall = new ModelRenderer(this, 0, 8);
            backWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            backWall.setRotationPoint(5.5f, 0f, 0f);
            backWall.rotateAngleY = PI / 2f; // 90 deg yaw
        }
        leftWall:
        {
            // will be scaled x2 at render
            float length = 10f;
            float height =  3f;
            float width  =  1f;
            leftWall = new ModelRenderer(this, 0, 12);
            leftWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            leftWall.setRotationPoint(0f, 0f, -4.5f);
        }
        rightWall:
        {
            // will be scaled x2 at render
            float length = 10f;
            float height =  3f;
            float width  =  1f;
            rightWall = new ModelRenderer(this, 0, 16);
            rightWall.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            rightWall.setRotationPoint(0f, 0f, 4.5f);
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
            //mainRotor.setRotationPoint(6f, -24f, 0f);
            mainRotor.setRotationPoint(3f, -11.7f, 0f); // halved to adjust for scale, and a little subtracted 
            
            mainRotor.rotateAngleY = .6f; // start off axis for realism
        }
        tailRotor:
        {
            // will be scaled x2 at render
            float length = 8f;
            float height = 1f;
            float width  = 0f;
            tailRotor = new ModelRenderer(this, 0, 2);
            tailRotor.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            tailRotor.setRotationPoint(17f, -7f, .7f);
            
            tailRotor.rotateAngleZ = .6f; // start off axis for realism
        }
        tail:
        {
            // will be scaled x2 at render
            float length = 10f;
            float height =  2f;
            float width  =  1f;
            tail = new ModelRenderer(this, 20, 26);
            tail.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            tail.setRotationPoint(13, -7, 0f);
        }
        rotorVerticalSupport:
        {
            float length =  4f;
            float height = 22f;
            float width  =  3f;
            rotor2 = new ModelRenderer(this, 50, 1);
            rotor2.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            rotor2.setRotationPoint(14f, -10f, 0f);
        }
        rotorHorizontalSupport:
        {
            float length = 12f;
            float height =  2f;
            float width  =  3f;
            rotor3 = new ModelRenderer(this, 20, 21);
            rotor3.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            rotor3.setRotationPoint(10, -22, 0f);
        }
        cockpit1:
        {
            // cockpit1 right vertical support
            float length =  1f;
            float height = 13f;
            float width  =  1f;
            cockpit1 = new ModelRenderer(this, 38, 1);
            cockpit1.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            cockpit1.setRotationPoint(-10f, -9f, 9f);
            cockpit1.rotateAngleY = PI / 2f;
        }
        cockpit2:
        {
            // cockpit2 left vertical support
            float length =  1f;
            float height = 13f;
            float width  =  1f;
            cockpit2 = new ModelRenderer(this, 42, 1);
            cockpit2.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            cockpit2.setRotationPoint(-10f, -9f, -9f);
        }
        cockpit3:
        {
            // cockpit3 horizontal support
            float length =  1f;
            float height = 19f;
            float width  =  1f;
            cockpit3 = new ModelRenderer(this, 46, 1);
            cockpit3.addBox(-length/2f, -height/2f, -width/2f, (int)length, (int)height, (int)width);
            cockpit3.setRotationPoint(-10f, -16f, 0f);
            cockpit3.rotateAngleX = PI / 2f;
            cockpit3.rotateAngleZ = PI / 2f;
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
        
        // rotor supports
        rotor2.render(scale);
        rotor3.render(scale);
        
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
