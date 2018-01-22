package mcjty.rftools.blocks.elevator;


import mcjty.lib.api.Infusable;
import mcjty.lib.container.BaseBlock;
import mcjty.lib.container.EmptyContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ElevatorBlock extends GenericRFToolsBlock<ElevatorTileEntity, EmptyContainer> implements Infusable {

    public ElevatorBlock() {
        super(Material.IRON, ElevatorTileEntity.class, EmptyContainer.class, "elevator", true);
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.HORIZROTATION;
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

    @Override
    public boolean hasRedstoneOutput() {
        return true;
    }

    @Override
    protected int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        EnumFacing direction = state.getValue(FACING_HORIZ);
        if (side == direction) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof ElevatorTileEntity) {
                ElevatorTileEntity elevatorTileEntity = (ElevatorTileEntity) te;
                return elevatorTileEntity.isPlatformHere() ? 15 : 0;
            }
        }
        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        super.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(ElevatorTileEntity.class, new ElevatorTESR());
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ELEVATOR;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiElevator> getGuiClass() {
        return GuiElevator.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This machine needs to be placed at a certain");
            list.add(TextFormatting.WHITE + "level pointing towards where a moving platform");
            list.add(TextFormatting.WHITE + "will be. Only the lowest elevator needs power");
            list.add(TextFormatting.WHITE + "Infusing bonus: reduced power consumption");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ElevatorTileEntity) {
            ElevatorTileEntity elevatorTileEntity = (ElevatorTileEntity) te;
            elevatorTileEntity.clearCaches(world.getBlockState(pos).getValue(BaseBlock.FACING_HORIZ));
        }
        if (placer instanceof EntityPlayer) {
            // @todo achievements
//            Achievements.trigger((EntityPlayer) placer, Achievements.goingUp);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ElevatorTileEntity) {
            ElevatorTileEntity elevatorTileEntity = (ElevatorTileEntity) te;
            elevatorTileEntity.clearCaches(state.getValue(BaseBlock.FACING_HORIZ));
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof ElevatorTileEntity) {
            ElevatorTileEntity elevatorTileEntity = (ElevatorTileEntity) te;
            probeInfo.text(TextFormatting.BLUE + "Name: " + elevatorTileEntity.getName());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        ElevatorTileEntity te = (ElevatorTileEntity) accessor.getTileEntity();
        int energy = te.getEnergyStored();
        currenttip.add(TextFormatting.GREEN + "RF: " + energy);
        if (te.getName() != null && !te.getName().isEmpty()) {
            currenttip.add(TextFormatting.BLUE + "Name: " + te.getName());
        }

        return currenttip;
    }
}
