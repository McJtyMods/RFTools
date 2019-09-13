package mcjty.rftools.blocks.booster;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;


public class BoosterSetup {
    public static BaseBlock boosterBlock;

    @ObjectHolder("rftools:booster")
    public static TileEntityType<?> TYPE_BOOSTER;

    public static void init() {
        boosterBlock = new BaseBlock("booster", new BlockBuilder()
                .tileEntitySupplier(BoosterTileEntity::new)
//                .container(BoosterTileEntity.CONTAINER_FACTORY)
//                .flags(BlockFlags.REDSTONE_CHECK)
                .infusable()
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.booster")) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };
    }

//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        boosterBlock.initModel();
//        boosterBlock.setGuiFactory(GuiBooster::new);
//    }
}
