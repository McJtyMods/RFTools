package mcjty.rftools.blocks.booster;

import mcjty.lib.compat.MyGameReg;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoosterSetup {
    public static BoosterBlock boosterBlock;

    public static void init() {
        boosterBlock = new BoosterBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        boosterBlock.initModel();
    }

    public static void initCrafting() {
        MyGameReg.addRecipe(new ItemStack(boosterBlock), "oDo", "GMI", "oEo", 'o', Items.GLASS_BOTTLE, 'M', ModBlocks.machineFrame,
                'D', Items.DIAMOND, 'E', Items.REDSTONE, 'G', Items.GOLD_INGOT, 'I', Items.IRON_INGOT);
    }
}
