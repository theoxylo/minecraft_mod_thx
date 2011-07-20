package net.minecraft.src;

import org.lwjgl.opengl.GL11;

public class ThxRenderAgent extends Render
{    
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;

    long prevTime;
    float deltaTime;

    public ThxRenderAgent()
    {
        float x_length = 8f;
        float z_width  = 2f;
        float y_height = 2f;

        compiled = false;
        displayList = 0;
        textureOffsetX = 0;
        textureOffsetY = 0;
        
        addBox(-x_length/2f, -y_height/2f, -z_width/2f, (int)x_length, (int)y_height, (int)z_width, 0f);
    }
    
    @Override
    public void doRender(Entity entityArg, double x, double y, double z, float yaw, float pitch)
    {
        deltaTimeCalc:
        {
	        long time = System.nanoTime();
	        deltaTime = (float) (time - prevTime) / 1000000000f; // convert to sec
	        prevTime = time;
	        // this happens when we haven't rendered in awhile (e.g. out of view)
	        if (deltaTime > .1f) deltaTime = .015f; // default to ~60 fps
	        
	        //System.out.println("ThxRenderAgent - deltaTime: " + deltaTime);
        }
    
        ThxEntity entity = null;
        try
        {
            entity = (ThxEntity) entityArg;
        }
        catch (ClassCastException e)
        {
            System.out.println("ThxEntity error: " + e);
            return;
        }
        if (entity == null) return;
        
        if (!entity.visible) return;
        
        GL11.glPushMatrix();
        
        GL11.glTranslatef((float)x, (float)y, (float)z);
        
        GL11.glRotatef(-90f - entity.rotationYaw, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(-entity.rotationPitch, 0.0f, 0.0f, 1.0f);
        GL11.glRotatef(-entity.rotationRoll, 1.0f, 0.0f, 0.0f);
        
        RenderEngine renderengine = renderManager.renderEngine;
        renderengine.bindTexture(renderengine.getTexture("/thx/missile.png"));

        GL11.glScalef(-1f, -1f, 1f);
        
        if(!compiled)
        {
	        displayList = GLAllocation.generateDisplayLists(1);
	        GL11.glNewList(displayList, 4864 /*GL_COMPILE*/);
	        Tessellator tessellator = Tessellator.instance;
	        for(int i = 0; i < faces.length; i++)
	        {
	            faces[i].draw(tessellator, .0625f);
	        }
	        GL11.glEndList();
	        compiled = true;
        }
        
        GL11.glPushMatrix();
        GL11.glTranslatef(rotationPointX * .0625f, rotationPointY * .0625f, rotationPointZ * .0625f);
        if(rotateAngleZ != 0.0F)
        {
            GL11.glRotatef(rotateAngleZ * 57.29578F, 0.0F, 0.0F, 1.0F);
        }
        if(rotateAngleY != 0.0F)
        {
            GL11.glRotatef(rotateAngleY * 57.29578F, 0.0F, 1.0F, 0.0F);
        }
        if(rotateAngleX != 0.0F)
        {
            GL11.glRotatef(rotateAngleX * 57.29578F, 1.0F, 0.0F, 0.0F);
        }
        GL11.glCallList(displayList);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
    
    // ModelRenderer

    public void addBox(float f, float f1, float f2, int i, int j, int k, float f3)
    {
        //corners = new PositionTextureVertex[8];
        faces = new TexturedQuad[6];
        float f4 = f + (float)i;
        float f5 = f1 + (float)j;
        float f6 = f2 + (float)k;
        f -= f3;
        f1 -= f3;
        f2 -= f3;
        f4 += f3;
        f5 += f3;
        f6 += f3;
        PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, f1, f2, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f4, f1, f2, 0.0F, 8F);
        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(f4, f5, f2, 8F, 8F);
        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(f, f5, f2, 8F, 0.0F);
        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, f1, f6, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f4, f1, f6, 0.0F, 8F);
        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(f4, f5, f6, 8F, 8F);
        PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(f, f5, f6, 8F, 0.0F);
        //corners[0] = positiontexturevertex;
        //corners[1] = positiontexturevertex1;
        //corners[2] = positiontexturevertex2;
        //corners[3] = positiontexturevertex3;
        //corners[4] = positiontexturevertex4;
        //corners[5] = positiontexturevertex5;
        //corners[6] = positiontexturevertex6;
        //corners[7] = positiontexturevertex7;
        faces[0] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex5, positiontexturevertex1, positiontexturevertex2, positiontexturevertex6
        }, textureOffsetX + k + i, textureOffsetY + k, textureOffsetX + k + i + k, textureOffsetY + k + j);
        faces[1] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex, positiontexturevertex4, positiontexturevertex7, positiontexturevertex3
        }, textureOffsetX + 0, textureOffsetY + k, textureOffsetX + k, textureOffsetY + k + j);
        faces[2] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex5, positiontexturevertex4, positiontexturevertex, positiontexturevertex1
        }, textureOffsetX + k, textureOffsetY + 0, textureOffsetX + k + i, textureOffsetY + k);
        faces[3] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex2, positiontexturevertex3, positiontexturevertex7, positiontexturevertex6
        }, textureOffsetX + k + i, textureOffsetY + 0, textureOffsetX + k + i + i, textureOffsetY + k);
        faces[4] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex1, positiontexturevertex, positiontexturevertex3, positiontexturevertex2
        }, textureOffsetX + k, textureOffsetY + k, textureOffsetX + k + i, textureOffsetY + k + j);
        faces[5] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex4, positiontexturevertex5, positiontexturevertex6, positiontexturevertex7
        }, textureOffsetX + k + i + k, textureOffsetY + k, textureOffsetX + k + i + k + i, textureOffsetY + k + j);
    }
    
    //private PositionTextureVertex corners[];
    private TexturedQuad faces[];
    private int textureOffsetX;
    private int textureOffsetY;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    private boolean compiled;
    private int displayList;
}
