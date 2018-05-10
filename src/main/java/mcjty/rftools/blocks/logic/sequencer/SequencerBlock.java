package mcjty.rftools.blocks.logic.sequencer;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.theoneprobe.TheOneProbeSupport;
import mcjty.theoneprobe.api.ElementAlignment;
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

public class SequencerBlock extends LogicSlabBlock<SequencerTileEntity, EmptyContainer> {

    public SequencerBlock() {
        super(RFTools.instance, Material.IRON, SequencerTileEntity.class, EmptyContainer.class, "sequencer_block", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiSequencer> getGuiClass() {
        return GuiSequencer.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int delay = tagCompound.getInteger("delay");
            list.add(TextFormatting.GREEN + "Delay: " + delay);
            long cycleBits = tagCompound.getLong("bits");

            int mode = tagCompound.getInteger("mode");
            String smode = SequencerMode.values()[mode].getDescription();
            list.add(TextFormatting.GREEN + "Mode: " + smode);

            list.add(TextFormatting.GREEN + "Bits: " + Long.toHexString(cycleBits));
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block emits a series of redstone");
            list.add(TextFormatting.WHITE + "signals in a pattern that you can set in the GUI.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof SequencerTileEntity) {
            SequencerTileEntity tileEntity = (SequencerTileEntity) te;
            IProbeInfo horizontal = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            horizontal.text(TextFormatting.GREEN + "Mode: " + tileEntity.getMode().getDescription());
            TheOneProbeSupport.addSequenceElement(horizontal, tileEntity.getCycleBits(),
                    tileEntity.getCurrentStep(), mode == ProbeMode.EXTENDED);
            int currentStep = tileEntity.getCurrentStep();
            boolean rc = tileEntity.checkOutput();
            probeInfo.text(TextFormatting.GREEN + "Step: " + TextFormatting.WHITE + currentStep +
            TextFormatting.GREEN + " -> " + TextFormatting.WHITE + (rc ? "on" : "off"));
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SEQUENCER;
    }
}
