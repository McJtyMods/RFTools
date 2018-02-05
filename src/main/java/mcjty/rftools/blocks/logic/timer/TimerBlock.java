package mcjty.rftools.blocks.logic.timer;

import mcjty.lib.container.EmptyContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.generic.LogicSlabBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class TimerBlock extends LogicSlabBlock<TimerTileEntity, EmptyContainer> {

    public TimerBlock() {
        super(Material.IRON, "timer_block", TimerTileEntity.class, EmptyContainer.class);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiTimer> getGuiClass() {
        return GuiTimer.class;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int delay = tagCompound.getInteger("delay");
            list.add(TextFormatting.GREEN + "Delay: " + delay);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block emits a redstone pulse");
            list.add(TextFormatting.WHITE + "after a certain amount of time unless it");
            list.add(TextFormatting.WHITE + "receives a redstone pulse itself before that.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }

    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof TimerTileEntity) {
            TimerTileEntity tileEntity = (TimerTileEntity) te;
            probeInfo.text(TextFormatting.GREEN + "Time: " + TextFormatting.WHITE + tileEntity.getTimer());
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_TIMER;
    }
}
