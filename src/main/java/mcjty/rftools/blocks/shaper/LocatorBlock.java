package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcjty.theoneprobe.apiimpl.styles.TextStyle;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class LocatorBlock extends GenericRFToolsBlock<LocatorTileEntity, EmptyContainer> /*, IRedstoneConnectable */ {

    public LocatorBlock() {
        super(Material.IRON, LocatorTileEntity.class, EmptyContainer.class, "locator", true);
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Place this block on top of a Scanner");
            list.add(TextFormatting.WHITE + "to extend its functionality with the ability");
            list.add(TextFormatting.WHITE + "to locate entities and machines using power");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        if (world.getBlockState(data.getPos().down()).getBlock() != BuilderSetup.scannerBlock) {
            probeInfo.text(TextStyleClass.ERROR + "Error! Needs a scanner below!");
        } else {
            probeInfo.text(TextStyleClass.INFO + "Scanner detected");
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiLocator.class;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_LOCATOR;
    }
}
