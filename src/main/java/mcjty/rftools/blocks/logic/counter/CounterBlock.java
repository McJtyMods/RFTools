package mcjty.rftools.blocks.logic.counter;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.network.clientinfo.PacketGetInfoFromServer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.generic.LogicSlabBlock;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class CounterBlock extends LogicSlabBlock<CounterTileEntity, EmptyContainer> {

    public CounterBlock() {
        super(Material.IRON, "counter_block", CounterTileEntity.class, EmptyContainer.class);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiCounter.class;
    }

    private static long lastTime = 0;

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int counter = tagCompound.getInteger("counter");
            list.add(TextFormatting.GREEN + "Counter: " + counter);
            int current = tagCompound.getInteger("current");
            list.add(TextFormatting.GREEN + "Current: " + current);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block counts redstone pulses and emits");
            list.add(TextFormatting.WHITE + "a signal once a certain number has been reached.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        CounterTileEntity counterTileEntity = (CounterTileEntity) worldIn.getTileEntity(pos);
        counterTileEntity.update();
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof CounterTileEntity) {
            CounterTileEntity counterTileEntity = (CounterTileEntity) te;
            probeInfo.text(TextFormatting.GREEN + "Current: " + counterTileEntity.getCurrent());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);

        if (System.currentTimeMillis() - lastTime > 500) {
            CounterTileEntity te = (CounterTileEntity) accessor.getTileEntity();
            lastTime = System.currentTimeMillis();
            RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID, new CounterInfoPacketServer(te.getWorld().provider.getDimension(),
                                                                                                                        te.getPos())));
        }

        currenttip.add(TextFormatting.GREEN + "Current: " + CounterInfoPacketClient.cntReceived);
        return currenttip;
    }


    @Override
    public int getGuiID() {
        return RFTools.GUI_COUNTER;
    }
}
