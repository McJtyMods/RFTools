package mcjty.rftools.wheelsupport;

import mcjty.lib.tools.InventoryTools;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class FindBlockClient {

    public static boolean pickBlockClient(World world, BlockPos pos, EntityPlayer player) {
        List<ItemStack> inventory = InventoryTools.getMainInventory(player);
        if (world.isAirBlock(pos)) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        ItemStack result = state.getBlock().getItem(world, pos, state);
        if (result == null || ItemStackTools.isEmpty(result)) {
            return false;
        }

        int slot = player.inventory.getSlotFor(result);
        if (slot != -1) {
            if (InventoryPlayer.isHotbar(slot)) {
                player.inventory.currentItem = slot;
            } else {
                Minecraft.getMinecraft().playerController.pickItem(slot);
            }
            return false;
        }

        return true;
    }
}
