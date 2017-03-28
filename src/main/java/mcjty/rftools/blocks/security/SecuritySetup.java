package mcjty.rftools.blocks.security;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class SecuritySetup {
    public static SecurityManagerBlock securityManagerBlock;

    public static SecurityCardItem securityCardItem;
    public static OrphaningCardItem orphaningCardItem;

    public static void init() {
        securityManagerBlock = new SecurityManagerBlock();
        orphaningCardItem = new OrphaningCardItem();
        securityCardItem = new SecurityCardItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        securityManagerBlock.initModel();
        orphaningCardItem.initModel();
        securityCardItem.initModel();
    }

    public static void initCrafting() {
        GameRegistry.addRecipe(new ItemStack(orphaningCardItem), " b ", "rir", " p ", 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                               'b', Items.BOOK, 'p', Items.PAPER);
        GameRegistry.addRecipe(new ItemStack(securityCardItem), " f ", "rir", " p ", 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'f', Items.FLINT, 'p', Items.PAPER);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(securityManagerBlock), "rfr", "fMf", "rcr", 'M', ModBlocks.machineFrame, 'r', Items.REDSTONE, 'f', Items.FLINT,
                'c', "chest"));
    }
}
