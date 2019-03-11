package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.ItemStackTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.proxy.GuiProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static mcjty.lib.varia.ItemStackTools.mapTag;

public class ItemFilterSetup {
    public static GenericBlock<ItemFilterTileEntity, GenericContainer> itemFilterBlock;

    public static void init() {
        itemFilterBlock = ModBlocks.builderFactory.<ItemFilterTileEntity> builder("item_filter")
                .tileEntityClass(ItemFilterTileEntity.class)
                .container(ItemFilterTileEntity.CONTAINER_FACTORY)
                .rotationType(BaseBlock.RotationType.NONE)
                .guiId(GuiProxy.GUI_ITEMFILTER)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.itemfilter")
                .infoExtendedParameter(stack -> {
                    int count = mapTag(stack, compound -> (int) ItemStackTools.getListStream(compound, "Items").filter(nbt -> !new ItemStack((NBTTagCompound)nbt).isEmpty()).count(), 0);
                    return Integer.toString(count);
                })
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        itemFilterBlock.initModel();
        itemFilterBlock.setGuiFactory(GuiItemFilter::new);
    }
}
