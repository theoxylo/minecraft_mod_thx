package net.minecraft.src;
import org.lwjgl.opengl.GL11;

public abstract class ThxModel //extends ModelBase
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;

    boolean visible = true;
    
    float deltaTime;
    long prevTime;
    long entityPrevTime;
    
    float rotationYaw;
    float rotationYawSpeed;
    
    float rotationPitch;
    float rotationPitchSpeed;
    
    float rotationRoll;
    float rotationRollSpeed;
    
    String renderTexture;
    RenderManager renderManager;
    
    StringBuilder log = new StringBuilder();
    
    int updateCount;
    

    /**
     *  deltaTime is used to render model movement more smoothly
     */
    public void update()
    {
        updateCount++;
        
	    long time = System.nanoTime();
        
	    deltaTime = ((float)(time - prevTime)) / 1000000000f; // divide by a billion to convert to sec
	    
        //log.append(toString() + ":\ttime " + time);
	    //log.append("\trotationYaw before " + rotationYaw);
	    //log.append("\tdeltaTime " + deltaTime);
	    
	    // this happens when we haven't rendered in awhile (e.g. out of view)
	    // -- was causing rotor to spin backward  sometimes. This fix 
	    // causes a different issue: the rotor only spins down when in view
        if (deltaTime > .1f)
        {
            //log.append("\t* fixing lag deltaTime of " + deltaTime + " to .03");
            
            deltaTime = .03f; // default to ~30 fps
        }
	    
        
        if (entityPrevTime > prevTime)
        {
            // if the entity was updated more recently than the model
            // sync the rotation yaw value and use adjusted delta for turn
            float adjustedDeltaTime = ((float)(time - entityPrevTime)) / 1000000000f; // divide by a billion to convert to sec
            rotationYaw += rotationYawSpeed * adjustedDeltaTime;
            //rotationPitch += rotationPitchSpeed * adjustedDeltaTime;
            //rotationRoll += rotationRollSpeed * adjustedDeltaTime;
            
            //entityPrevTime = 0L; // reset until updated from entity again
        }
        else 
        {
            rotationYaw   += rotationYawSpeed   * deltaTime;
            //rotationPitch += rotationPitchSpeed * deltaTime;
            //rotationRoll  += rotationRollSpeed  * deltaTime;
        }
	    prevTime = time;
        
        GL11.glRotatef(-90f - rotationYaw, 0.0f, 1.0f, 0.0f);
        
        GL11.glRotatef(-rotationPitch, 0.0f, 0.0f, 1.0f);
        GL11.glRotatef(-rotationRoll, 1.0f, 0.0f, 0.0f);
        
        GL11.glScalef(-1f, -1f, 1f);
        
	    //log.append("\trotationYawSpeed " + rotationYawSpeed);
	    //log.append("\trotationYaw after " + rotationYaw);
	    //log.append("\n");
	    
	    //if (updateCount % 300 == 0) // flush log every ~5 second
	    //{
		    //log.append("\n");
	        //System.out.println(log);
	        
	        //log.setLength(0);
	    //}
    }
    
    abstract void render();
}
