package mcjty.rftools.blocks.infuser;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class MachineInfuserSetup {
    public static MachineInfuserBlock machineInfuserBlock;

    public static void setupBlocks() {
        machineInfuserBlock = new MachineInfuserBlock();
        GameRegistry.registerBlock(machineInfuserBlock, GenericItemBlock.class, "machineInfuserBlock");
        GameRegistry.registerTileEntity(MachineInfuserTileEntity.class, "MachineInfuserTileEntity");
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(machineInfuserBlock), "srs", "dMd", "srs", 'M', ModBlocks.machineFrame, 's', DimletSetup.dimensionalShard,
                'r', Items.redstone, 'd', Items.diamond);
    }
}
