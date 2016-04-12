package mcjty.rftools.blocks.security;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        GameRegistry.addRecipe(new ItemStack(orphaningCardItem), " b ", "rir", " p ", 'r', Items.redstone, 'i', Items.iron_ingot,
                               'b', Items.book, 'p', Items.paper);
        GameRegistry.addRecipe(new ItemStack(securityCardItem), " f ", "rir", " p ", 'r', Items.redstone, 'i', Items.iron_ingot,
                'f', Items.flint, 'p', Items.paper);
        GameRegistry.addRecipe(new ItemStack(securityManagerBlock), "rfr", "fMf", "rcr", 'M', ModBlocks.machineFrame, 'r', Items.redstone, 'f', Items.flint,
                'c', Blocks.chest);
    }
}
