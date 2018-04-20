package mcjty.rftools.blocks.booster;

import mcjty.lib.builder.BlockFlags;
import mcjty.lib.container.BaseBlock;
import mcjty.lib.container.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoosterSetup {
    public static GenericBlock<BoosterTileEntity, GenericContainer> boosterBlock;

    public static void init() {
        boosterBlock = ModBlocks.builderFactory.<BoosterTileEntity, GenericContainer> builder("booster")
                .tileEntityClass(BoosterTileEntity.class)
                .container(GenericContainer.class, BoosterTileEntity.CONTAINER_FACTORY)
                .rotationType(BaseBlock.RotationType.NONE)
                .flags(BlockFlags.REDSTONE_CHECK)
                .guiId(RFTools.GUI_BOOSTER)
                .moduleSupport(BoosterTileEntity.MODULE_SUPPORT)
                .information("message.rftools.shiftmessage")
                .informationShift("message.rftools.booster")
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        boosterBlock.initModel();
        boosterBlock.setGuiClass(GuiBooster.class);
    }
}
