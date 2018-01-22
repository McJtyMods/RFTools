package mcjty.rftools.blocks.logic.invchecker;

import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.generic.LogicSlabBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class InvCheckerBlock extends LogicSlabBlock<InvCheckerTileEntity, InvCheckerContainer> {

    public InvCheckerBlock() {
        super(Material.IRON, "invchecker_block", InvCheckerTileEntity.class, InvCheckerContainer.class);
    }

    @Override
    public boolean needsRedstoneCheck() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiInvChecker> getGuiClass() {
        return GuiInvChecker.class;
    }

    private static long lastTime = 0;

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
//        NBTTagCompound tagCompound = itemStack.getTagCompound();
//        if (tagCompound != null) {
//            int counter = tagCompound.getInteger("counter");
//            list.add(TextFormatting.GREEN + "Counter: " + counter);
//            int current = tagCompound.getInteger("current");
//            list.add(TextFormatting.GREEN + "Current: " + current);
//        }
//
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block gives a redstone signal");
            list.add(TextFormatting.WHITE + "if the amount of items in an inventory slot.");
            list.add(TextFormatting.WHITE + "exceeds a certain value");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof InvCheckerTileEntity) {
            InvCheckerTileEntity counterTileEntity = (InvCheckerTileEntity) te;
            boolean rc = counterTileEntity.checkOutput();
            probeInfo.text(TextFormatting.GREEN + "Output: " + TextFormatting.WHITE + (rc ? "on" : "off"));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
//
//        if (System.currentTimeMillis() - lastTime > 500) {
//            CounterTileEntity te = (CounterTileEntity) accessor.getTileEntity();
//            lastTime = System.currentTimeMillis();
//            RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID, new CounterInfoPacketServer(te.getWorld().provider.getDimension(),
//                                                                                                                        te.getPos())));
//        }
//
//        currenttip.add(TextFormatting.GREEN + "Current: " + CounterInfoPacketClient.cntReceived);
        return currenttip;
    }


    @Override
    public int getGuiID() {
        return RFTools.GUI_INVCHECKER;
    }
}
