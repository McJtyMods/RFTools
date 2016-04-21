package mcjty.rftools.blocks.crafter;

import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CrafterSetup {

    public static CrafterBlock crafterBlock1;
    public static CrafterBlock crafterBlock2;
    public static CrafterBlock crafterBlock3;

    public static void init() {
        crafterBlock1 = new CrafterBlock("crafter1", CrafterBlockTileEntity1.class);
        crafterBlock2 = new CrafterBlock("crafter2", CrafterBlockTileEntity2.class);
        crafterBlock3 = new CrafterBlock("crafter3", CrafterBlockTileEntity3.class);
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        crafterBlock1.initModel();
        crafterBlock2.initModel();
        crafterBlock3.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;

        GameRegistry.addRecipe(new ItemStack(crafterBlock1), " T ", "cMc", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'c', Blocks.CRAFTING_TABLE);
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(redstoneTorch), null,
                new ItemStack(Blocks.CRAFTING_TABLE), new ItemStack(crafterBlock1), new ItemStack(Blocks.CRAFTING_TABLE),
                null, new ItemStack(redstoneTorch), null
        }, new ItemStack(crafterBlock2), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                null, new ItemStack(redstoneTorch), null,
                new ItemStack(Blocks.CRAFTING_TABLE), new ItemStack(crafterBlock2), new ItemStack(Blocks.CRAFTING_TABLE),
                null, new ItemStack(redstoneTorch), null
        }, new ItemStack(crafterBlock3), 4));
    }
}
