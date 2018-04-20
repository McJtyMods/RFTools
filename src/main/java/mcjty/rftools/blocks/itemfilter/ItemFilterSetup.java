package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.container.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFilterSetup {
    public static GenericBlock<ItemFilterTileEntity, GenericContainer> itemFilterBlock;

    public static void init() {
        itemFilterBlock = ModBlocks.builderFactory.<ItemFilterTileEntity, GenericContainer> builder("item_filter")
                .tileEntityClass(ItemFilterTileEntity.class)
                .container(GenericContainer.class, ItemFilterTileEntity.CONTAINER_FACTORY)
                .guiId(RFTools.GUI_ITEMFILTER)
                .information("message.rftools.shiftmessage")
                .informationShift("message.rftools.itemfilter", stack -> {
                    int count = 0;
                    NBTTagCompound tagCompound = stack.getTagCompound();
                    if (tagCompound != null) {
                        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
                        count = bufferTagList.tagCount();
                    }
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
