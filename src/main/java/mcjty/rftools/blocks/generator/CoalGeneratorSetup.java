package mcjty.rftools.blocks.generator;

import mcjty.lib.compat.MyGameReg;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CoalGeneratorSetup {
    public static CoalGeneratorBlock coalGeneratorBlock;

    public static void init() {
        coalGeneratorBlock = new CoalGeneratorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        coalGeneratorBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;

        MyGameReg.addRecipe(new ItemStack(coalGeneratorBlock), "cTc", "cMc", "cTc", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'c', Items.COAL);
    }

}
