package mcjty.rftools.blocks.generator;

import mcjty.lib.builder.BlockFlags;
import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.setup.GuiProxy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CoalGeneratorSetup {
    public static GenericBlock<CoalGeneratorTileEntity, GenericContainer> coalGeneratorBlock;

    public static void init() {
        if(CoalGeneratorConfiguration.enabled) {
            coalGeneratorBlock = ModBlocks.builderFactory.<CoalGeneratorTileEntity> builder("coalgenerator")
                    .tileEntityClass(CoalGeneratorTileEntity.class)
                    .container(CoalGeneratorTileEntity.CONTAINER_FACTORY)
                    .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.RENDER_SOLID, BlockFlags.RENDER_CUTOUT)
                    .property(CoalGeneratorTileEntity.WORKING)
                    .guiId(GuiProxy.GUI_COALGENERATOR)
                    .infusable()
                    .info("message.rftools.shiftmessage")
                    .infoExtended("message.rftools.coalgenerator")
                    .infoExtendedParameter(stack -> Long.toString(CoalGeneratorConfiguration.rfPerTick))
                    .build();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        if(CoalGeneratorConfiguration.enabled) {
            coalGeneratorBlock.initModel();
            coalGeneratorBlock.setGuiFactory(GuiCoalGenerator::new);
        }
    }
}
