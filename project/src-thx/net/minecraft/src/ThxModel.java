package net.minecraft.src;
import org.lwjgl.opengl.GL11;

public abstract class ThxModel //extends ModelBase
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;

    boolean visible = true;
    
    long prevTime;
    float deltaTime;
    
    float rotationYaw;
    float rotationYawSpeed;
    
    float rotationPitch;
    float rotationRoll;
    
    String renderTexture;
    RenderManager renderManager;

    /**
     *  deltaTime is used to render model movement more smoothly
     */
    public void update()
    {
	    long time = System.nanoTime();
	    deltaTime = (float) (time - prevTime) / 1000000000f; // convert to sec
	    prevTime = time;
	    
	    // this happens when we haven't rendered in awhile (e.g. out of view)
	    // -- was causeing rotor to spin backward upon sometimes. This fix 
	    // causes a different issue: the rotor only spins down when in view
	    if (deltaTime > .1f) deltaTime = .03f; // default to ~30 fps
	    
	    rotationYaw += rotationYawSpeed * deltaTime;
        GL11.glRotatef(-90f - rotationYaw, 0.0f, 1.0f, 0.0f);
        
        GL11.glRotatef(-rotationPitch, 0.0f, 0.0f, 1.0f);
        GL11.glRotatef(-rotationRoll, 1.0f, 0.0f, 0.0f);
        
        RenderEngine renderengine = renderManager.renderEngine;
        renderengine.bindTexture(renderengine.getTexture(renderTexture));

        GL11.glScalef(-1f, -1f, 1f);
    }
    
    abstract void render();
}
