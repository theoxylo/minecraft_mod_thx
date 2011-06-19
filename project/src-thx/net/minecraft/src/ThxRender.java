package net.minecraft.src;

import org.lwjgl.opengl.GL11;

public class ThxRender extends Render
{
    @Override
    public void doRender(Entity entityArg, double x, double y, double z, float yaw, float pitch)
    {
        ThxEntity entity = null;
        
        try
        {
            entity = (ThxEntity) entityArg;
        }
        catch (ClassCastException e)
        {
            return;
        }
        
        //if (!entity.visible) return;
        
        GL11.glPushMatrix();
        
        GL11.glTranslatef((float)x, (float)y, (float)z);
        
        GL11.glRotatef(-90f - entity.rotationYaw, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(-entity.rotationPitch, 0.0f, 0.0f, 1.0f);
        GL11.glRotatef(-entity.rotationRoll, 1.0f, 0.0f, 0.0f);
        
        loadTexture(entity.renderTexture);
        GL11.glScalef(-1f, -1f, 1f);
        
        entity.model.render(0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0625f);
        
        GL11.glPopMatrix();
    }
}
