package mcjty.rftools.blocks.infuser;

import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.items.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MachineInfuserSetup {
    public static MachineInfuserBlock machineInfuserBlock;

    public static void init() {
        machineInfuserBlock = new MachineInfuserBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        machineInfuserBlock.initModel();
    }

    public static void initCrafting() {
        GameRegistry.addRecipe(new ItemStack(machineInfuserBlock), "srs", "dMd", "srs", 'M', ModBlocks.machineFrame, 's', ModItems.dimensionalShardItem,
                'r', Items.REDSTONE, 'd', Items.DIAMOND);
    }
}
