package mcjty.rftools.blocks.endergen;

import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class EndergenicSetup {
    public static EndergenicBlock endergenicBlock;
    public static PearlInjectorBlock pearlInjectorBlock;
    public static EnderMonitorBlock enderMonitorBlock;

    public static void init() {
        endergenicBlock = new EndergenicBlock();
        pearlInjectorBlock = new PearlInjectorBlock();
        enderMonitorBlock = new EnderMonitorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        endergenicBlock.initModel();
        pearlInjectorBlock.initModel();
        enderMonitorBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;
        if (GeneralConfiguration.enableEndergenRecipe) {
            GameRegistry.addRecipe(new ItemStack(endergenicBlock), "DoD", "oMo", "DoD", 'M', ModBlocks.machineFrame, 'D', Items.DIAMOND, 'o', Items.ENDER_PEARL);
        }
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(pearlInjectorBlock), " C ", "rMr", " H ", 'C', "chest", 'r', Items.REDSTONE,
                'M', ModBlocks.machineFrame, 'H', Blocks.HOPPER));
        GameRegistry.addRecipe(new ItemStack(enderMonitorBlock), "ror", "TMT", "rTr", 'o', Items.ENDER_PEARL, 'r', Items.REDSTONE, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
    }
}
