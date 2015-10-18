package mcjty.rftools.blocks.crafter;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.lib.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CrafterSetup {
    public static CrafterBlock crafterBlock1;
    public static CrafterBlock crafterBlock2;
    public static CrafterBlock crafterBlock3;

    public static void setupBlocks() {
        crafterBlock1 = new CrafterBlock("crafterBlock1", "machineCrafter1", CrafterBlockTileEntity1.class);
        GameRegistry.registerBlock(crafterBlock1, GenericItemBlock.class, "crafterBlock1");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity1.class, "CrafterTileEntity1");

        crafterBlock2 = new CrafterBlock("crafterBlock2", "machineCrafter2", CrafterBlockTileEntity2.class);
        GameRegistry.registerBlock(crafterBlock2, GenericItemBlock.class, "crafterBlock2");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity2.class, "CrafterTileEntity2");

        crafterBlock3 = new CrafterBlock("crafterBlock3", "machineCrafter3", CrafterBlockTileEntity3.class);
        GameRegistry.registerBlock(crafterBlock3, GenericItemBlock.class, "crafterBlock3");
        GameRegistry.registerTileEntity(CrafterBlockTileEntity3.class, "CrafterTileEntity3");
    }

    public static void setupCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");

        GameRegistry.addRecipe(new ItemStack(crafterBlock1), " T ", "cMc", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'c', Blocks.crafting_table);
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack((Item) redstoneTorch), null,
                new ItemStack(Blocks.crafting_table), new ItemStack(crafterBlock1), new ItemStack(Blocks.crafting_table),
                null, new ItemStack((Item) redstoneTorch), null
        }, new ItemStack(crafterBlock2), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack((Item) redstoneTorch), null,
                new ItemStack(Blocks.crafting_table), new ItemStack(crafterBlock2), new ItemStack(Blocks.crafting_table),
                null, new ItemStack((Item) redstoneTorch), null
        }, new ItemStack(crafterBlock3), 4));
    }
}
