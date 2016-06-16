package mcjty.rftools.blocks.logic;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
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

public class InvCheckerBlock extends LogicSlabBlock<InvCheckerTileEntity, EmptyContainer> {

    public InvCheckerBlock() {
        super(Material.IRON, "invchecker_block", InvCheckerTileEntity.class, EmptyContainer.class);
    }

    @Override
    public boolean needsRedstoneCheck() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiInvChecker.class;
    }

    private static long lastTime = 0;

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
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
//        TileEntity te = world.getTileEntity(data.getPos());
//        if (te instanceof CounterTileEntity) {
//            CounterTileEntity counterTileEntity = (CounterTileEntity) te;
//            probeInfo.text(TextFormatting.GREEN + "Current: " + counterTileEntity.getCurrent());
//        }
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
