package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.ItemStackTools;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

import static mcjty.lib.varia.ItemStackTools.mapTag;

public class ItemFilterSetup {
    public static BaseBlock itemFilterBlock;

    @ObjectHolder("rftools:item_filter")
    public static TileEntityType<?> TYPE_ITEM_FILTER;

    public static void init() {
        itemFilterBlock = new BaseBlock("item_filter", new BlockBuilder()
                .tileEntitySupplier(ItemFilterTileEntity::new)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.itemfilter")
                .infoExtendedParameter(stack -> {
                    int count = mapTag(stack, compound -> (int) ItemStackTools.getListStream(compound, "Items").filter(nbt -> !ItemStack.read((CompoundNBT)nbt).isEmpty()).count(), 0);
                    return Integer.toString(count);
                })) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        itemFilterBlock.initModel();
//        itemFilterBlock.setGuiFactory(GuiItemFilter::new);
//    }
}
