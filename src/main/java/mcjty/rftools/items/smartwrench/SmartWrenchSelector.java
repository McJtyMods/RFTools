package mcjty.rftools.items.smartwrench;

import net.minecraft.entity.player.EntityPlayer;

public interface SmartWrenchSelector {

    /**
     * This is only called server side. Select a block for this tile entity.
     */
    void selectBlock(EntityPlayer player, int x, int y, int z);
}
