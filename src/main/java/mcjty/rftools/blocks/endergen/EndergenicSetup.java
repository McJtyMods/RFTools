package mcjty.rftools.blocks.endergen;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.ItemStackTools;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

import static mcjty.lib.varia.ItemStackTools.mapTag;

public class EndergenicSetup {
    public static BaseBlock endergenicBlock;
    public static BaseBlock pearlInjectorBlock;
    public static EnderMonitorBlock enderMonitorBlock;

    @ObjectHolder("rftools:ender_monitor")
    public static TileEntityType<?> TYPE_ENDER_MONITOR;

    @ObjectHolder("rftools:endergenic")
    public static TileEntityType<?> TYPE_ENDERGENIC;

    @ObjectHolder("rftools:pearl_injector")
    public static TileEntityType<?> TYPE_PEARL_INJECTOR;


    public static void init() {
        endergenicBlock = new BaseBlock("endergenic", new BlockBuilder()
                .tileEntitySupplier(EndergenicTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.NON_OPAQUE, BlockFlags.RENDER_TRANSLUCENT)
                .infusable()
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.endergenic")) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };

        pearlInjectorBlock = new BaseBlock("pearl_injector", new BlockBuilder()
                .tileEntitySupplier(PearlInjectorTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.pearl_injector")
                .infoExtendedParameter(stack -> {
                    int count = mapTag(stack, compound -> (int) ItemStackTools.getListStream(compound, "Items").filter(nbt -> !ItemStack.read((CompoundNBT)nbt).isEmpty()).count(), 0);
                    return Integer.toString(count);
                }));

        enderMonitorBlock = new EnderMonitorBlock();
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        endergenicBlock.initModel();
//        endergenicBlock.setGuiFactory(GuiEndergenic::new);
//        EndergenicRenderer.register();
//
//        pearlInjectorBlock.initModel();
//        pearlInjectorBlock.setGuiFactory(GuiPearlInjector::new);
//
//        enderMonitorBlock.initModel();
//    }
}
