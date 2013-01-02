package net.minecraft.src;

import org.lwjgl.opengl.GL11;

public abstract class ThxModel extends ModelBase
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;

    boolean visible = true;
    
    boolean paused;
    
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
    
    int updateCount;
    

    /**
     *  deltaTime is used to render model rotation more smoothly
     */
    public void update()
    {
        updateCount++;
        
	    long time = System.nanoTime();
        
	    deltaTime = ((float)(time - prevTime)) / 1000000000f; // divide by a billion to convert to sec
	    
	    // this happens when we haven't rendered in awhile (e.g. out of view)
        if (deltaTime > .03f) deltaTime = .03f; // default to ~30 fps
	    
        if (paused)
        {
            // do nothing
        }
        else if (entityPrevTime > prevTime)
        {
            // if the entity was updated more recently than the model
            // use adjusted delta (leave deltaTime alone so subclass models can update
            // parts that may be independent of entity, e.g. helicopter rotor)
            float adjustedDeltaTime = ((float)(time - entityPrevTime)) / 1000000000f; // divide by a billion to convert to sec
            rotationYaw += rotationYawSpeed * adjustedDeltaTime;
            rotationPitch += rotationPitchSpeed * adjustedDeltaTime;
            rotationRoll += rotationRollSpeed * adjustedDeltaTime;
        }
        else 
        {
            rotationYaw   += rotationYawSpeed   * deltaTime;
            rotationPitch += rotationPitchSpeed * deltaTime;
            rotationRoll  += rotationRollSpeed  * deltaTime;
        }
	    prevTime = time;
        
        GL11.glRotatef(-90f - rotationYaw, 0.0f, 1.0f, 0.0f); // why -90 adjustment?
        
        GL11.glRotatef(-rotationPitch, 0.0f, 0.0f, 1.0f);
        GL11.glRotatef(-rotationRoll, 1.0f, 0.0f, 0.0f);
        
        GL11.glScalef(-1f, -1f, 1f);
    }
    
    abstract void render();
}
