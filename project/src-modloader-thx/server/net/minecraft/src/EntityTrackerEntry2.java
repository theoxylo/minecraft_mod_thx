package net.minecraft.src;

public class EntityTrackerEntry2
{
    public int entityId = -1;
    public boolean entityHasOwner;

    public EntityTrackerEntry2(int entityId, boolean entityHasOwner)
    {
        System.out.println("EntityTrackerEntry2 called with entityId: " + entityId);
        
        this.entityId = entityId;
        this.entityHasOwner = entityHasOwner;
    }
}
