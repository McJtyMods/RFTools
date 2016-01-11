package mcjty.rftools.blocks.logic;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SequencerBlock extends LogicSlabBlock<SequencerTileEntity, EmptyContainer> {

    public SequencerBlock() {
        super(Material.iron, "sequencer_block", SequencerTileEntity.class, EmptyContainer.class);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiSequencer.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int delay = tagCompound.getInteger("delay");
            list.add(EnumChatFormatting.GREEN + "Delay: " + delay);
            long cycleBits = tagCompound.getLong("bits");

            int mode = tagCompound.getInteger("mode");
            String smode = SequencerMode.values()[mode].getDescription();
            list.add(EnumChatFormatting.GREEN + "Mode: " + smode);

            list.add(EnumChatFormatting.GREEN + "Bits: " + Long.toHexString(cycleBits));
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This logic block emits a series of redstone");
            list.add(EnumChatFormatting.WHITE + "signals in a pattern that you can set in the GUI.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SEQUENCER;
    }
}
