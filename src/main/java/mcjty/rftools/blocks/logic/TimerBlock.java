package mcjty.rftools.blocks.logic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.lib.container.EmptyContainer;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class TimerBlock extends LogicSlabBlock {

    public TimerBlock() {
        super(Material.iron, "timerBlock", TimerTileEntity.class);
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int delay = tagCompound.getInteger("delay");
            list.add(EnumChatFormatting.GREEN + "Delay: " + delay);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This logic block emits a redstone pulse");
            list.add(EnumChatFormatting.WHITE + "after a certain amount of time unless it");
            list.add(EnumChatFormatting.WHITE + "receives a redstone pulse itself before that.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }

    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_TIMER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        TimerTileEntity timerTileEntity = (TimerTileEntity) tileEntity;
        return new GuiTimer(timerTileEntity, new EmptyContainer(entityPlayer));
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineTimerTop";
    }
}
