package mcjty.rftools.blocks.endergen;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.builder.BlockFlags;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.ItemStackTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static mcjty.lib.varia.ItemStackTools.mapTag;

public class EndergenicSetup {
    public static GenericBlock<EndergenicTileEntity, GenericContainer> endergenicBlock;
    public static GenericBlock<PearlInjectorTileEntity, GenericContainer> pearlInjectorBlock;
    public static EnderMonitorBlock enderMonitorBlock;

    public static void init() {
        endergenicBlock = ModBlocks.builderFactory.<EndergenicTileEntity> builder("endergenic")
                .tileEntityClass(EndergenicTileEntity.class)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.NON_OPAQUE, BlockFlags.RENDER_TRANSLUCENT)
                .rotationType(BaseBlock.RotationType.NONE)
                .infusable()
                .guiId(RFTools.GUI_ENDERGENIC)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.endergenic")
                .build();

        pearlInjectorBlock = ModBlocks.builderFactory.<PearlInjectorTileEntity> builder("pearl_injector")
                .tileEntityClass(PearlInjectorTileEntity.class)
                .container(PearlInjectorTileEntity.CONTAINER_FACTORY)
                .flags(BlockFlags.REDSTONE_CHECK)
                .guiId(RFTools.GUI_PEARL_INJECTOR)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.pearl_injector")
                .infoExtendedParameter(stack -> {
                    int count = mapTag(stack, compound -> (int) ItemStackTools.getListStream(compound, "Items").filter(nbt -> !new ItemStack((NBTTagCompound)nbt).isEmpty()).count(), 0);
                    return Integer.toString(count);
                })
                .build();

        enderMonitorBlock = new EnderMonitorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        endergenicBlock.initModel();
        endergenicBlock.setGuiFactory(GuiEndergenic::new);
        EndergenicRenderer.register();

        pearlInjectorBlock.initModel();
        pearlInjectorBlock.setGuiFactory(GuiPearlInjector::new);

        enderMonitorBlock.initModel();
    }
}
