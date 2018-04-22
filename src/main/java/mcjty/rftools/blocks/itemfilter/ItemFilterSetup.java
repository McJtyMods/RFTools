package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.container.BaseBlock;
import mcjty.lib.container.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.ItemStackTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
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
                .guiId(RFTools.GUI_ITEMFILTER)
                .information("message.rftools.shiftmessage")
                .informationShift("message.rftools.itemfilter", stack -> {
                    int count = mapTag(stack, compound -> (int) ItemStackTools.getListStream(compound, "Items").filter(nbt -> !new ItemStack((NBTTagCompound)nbt).isEmpty()).count(), 0);
                    return Integer.toString(count);
                })
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        itemFilterBlock.initModel();
        itemFilterBlock.setGuiClass(GuiItemFilter.class);
    }
}
