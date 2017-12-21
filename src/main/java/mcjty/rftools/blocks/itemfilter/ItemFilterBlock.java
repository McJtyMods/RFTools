package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ItemFilterBlock extends GenericRFToolsBlock<ItemFilterTileEntity, ItemFilterContainer> {

    public ItemFilterBlock() {
        super(Material.IRON, ItemFilterTileEntity.class, ItemFilterContainer.class, "item_filter", true);
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiItemFilter.class;
    }

//    @Override
//    public void registerBlockIcons(IIconRegister iconRegister) {
//        icons[ForgeDirection.DOWN.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterD");
//        icons[ForgeDirection.UP.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterU");
//        icons[ForgeDirection.NORTH.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterN");
//        icons[ForgeDirection.SOUTH.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterS");
//        icons[ForgeDirection.WEST.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterW");
//        icons[ForgeDirection.EAST.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterE");
//    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            list.add(TextFormatting.GREEN + "Contents: " + bufferTagList.tagCount() + " stacks");
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "With this block you can direct items from any side");
            list.add(TextFormatting.WHITE + "to any other side. This allows you to make item");
            list.add(TextFormatting.WHITE + "filters for quarries, tree farms, mob farms, ...");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }

    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ITEMFILTER;
    }
}
