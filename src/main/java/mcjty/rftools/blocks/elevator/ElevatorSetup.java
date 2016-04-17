package mcjty.rftools.blocks.elevator;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ElevatorSetup {
    public static ElevatorBlock elevatorBlock;

    public static void init() {
        elevatorBlock = new ElevatorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        elevatorBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.redstone_torch;

        GameRegistry.addRecipe(new ItemStack(elevatorBlock), "cec", "cMc", "cTc", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'c', Items.redstone, 'e', Items.ender_pearl);
    }

}
