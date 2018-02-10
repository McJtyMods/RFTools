package mcjty.rftools.blocks.shaper;

import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ScannerBlock extends GenericRFToolsBlock<ScannerTileEntity, ScannerContainer> /*, IRedstoneConnectable */ {

    public ScannerBlock() {
        super(Material.IRON, ScannerTileEntity.class, ScannerContainer.class, "scanner", true);
    }

    public ScannerBlock(Class<? extends ScannerTileEntity> tileEntityClass,
                        Class<? extends ScannerContainer> containerClass,
                        String name) {
        super(Material.IRON, tileEntityClass, containerClass, name, true);
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (itemStack.getTagCompound() != null) {
            int scanId = itemStack.getTagCompound().getInteger("scanid");
            list.add(TextFormatting.DARK_GREEN + "Scan id: " + scanId);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This block can scan an area and link");
            list.add(TextFormatting.WHITE + "to shape cards for the Builder or Shield.");
            list.add(TextFormatting.WHITE + "The resulting shape card can also be used");
            list.add(TextFormatting.WHITE + "in the Composer");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof ScannerTileEntity) {
            probeInfo.text(TextStyleClass.LABEL + "Scan id: " + TextStyleClass.INFO + ((ScannerTileEntity) te).getScanId());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof ScannerTileEntity) {
            currenttip.add("Scan id: " + TextFormatting.WHITE + ((ScannerTileEntity) te).getScanId());
        }
        return currenttip;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiScanner> getGuiClass() {
        return GuiScanner.class;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SCANNER;
    }
}
