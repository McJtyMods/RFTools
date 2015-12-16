package mcjty.rftools.items.smartwrench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

public interface SmartWrenchSelector {

    /**
     * This is only called server side. Select a block for this tile entity.
     */
    void selectBlock(EntityPlayer player, BlockPos pos);
}
