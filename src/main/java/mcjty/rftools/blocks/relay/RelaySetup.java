package mcjty.rftools.blocks.relay;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RelaySetup {
    public static RelayBlock relayBlock;

    public static void init() {
        relayBlock = new RelayBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        relayBlock.initModel();
    }


    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;
        GameRegistry.addRecipe(new ItemStack(relayBlock), "gTg", "gMg", "gTg", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'g', Items.GOLD_INGOT);
    }
}
