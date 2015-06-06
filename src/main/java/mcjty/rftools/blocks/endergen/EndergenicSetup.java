package mcjty.rftools.blocks.endergen;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class EndergenicSetup {
    public static EndergenicBlock endergenicBlock;
    public static PearlInjectorBlock pearlInjectorBlock;
    public static EnderMonitorBlock enderMonitorBlock;

    public static void setupBlocks() {
        endergenicBlock = new EndergenicBlock();
        GameRegistry.registerBlock(endergenicBlock, GenericItemBlock.class, "endergenicBlock");
        GameRegistry.registerTileEntity(EndergenicTileEntity.class, "EndergenicTileEntity");

        pearlInjectorBlock = new PearlInjectorBlock();
        GameRegistry.registerBlock(pearlInjectorBlock, GenericItemBlock.class, "pearlInjectorBlock");
        GameRegistry.registerTileEntity(PearlInjectorTileEntity.class, "PearlInjectorTileEntity");

        enderMonitorBlock = new EnderMonitorBlock();
        GameRegistry.registerBlock(enderMonitorBlock, GenericItemBlock.class, "enderMonitorBlock");
        GameRegistry.registerTileEntity(EnderMonitorTileEntity.class, "EnderMonitorTileEntity");
    }

    public static void setupCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        if (GeneralConfiguration.enableEndergenRecipe) {
            GameRegistry.addRecipe(new ItemStack(endergenicBlock), "DoD", "oMo", "DoD", 'M', ModBlocks.machineFrame, 'D', Items.diamond, 'o', Items.ender_pearl);
        }
        GameRegistry.addRecipe(new ItemStack(pearlInjectorBlock), " C ", "rMr", " H ", 'C', Blocks.chest, 'r', Items.redstone,
                'M', ModBlocks.machineFrame, 'H', Blocks.hopper);
        GameRegistry.addRecipe(new ItemStack(enderMonitorBlock), "ror", "TMT", "rTr", 'o', Items.ender_pearl, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
    }
}
