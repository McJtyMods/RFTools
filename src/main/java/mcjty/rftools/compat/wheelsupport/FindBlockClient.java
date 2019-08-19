package mcjty.rftools.compat.wheelsupport;

import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class FindBlockClient {

    public static boolean pickBlockClient(World world, BlockPos pos, PlayerEntity player) {
        List<ItemStack> inventory = Collections.unmodifiableList(player.inventory.mainInventory);
        if (world.isAirBlock(pos)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        ItemStack result = state.getBlock().getItem(world, pos, state);
        if (result == null || result.isEmpty()) {
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
