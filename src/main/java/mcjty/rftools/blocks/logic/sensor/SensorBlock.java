package mcjty.rftools.blocks.logic.sensor;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.rftools.RFTools;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SensorBlock extends LogicSlabBlock<SensorTileEntity, SensorContainer> {

    public SensorBlock() {
        super(RFTools.instance, Material.IRON, SensorTileEntity.class, SensorContainer.class, "sensor_block", false);
    }

    @Override
    public boolean needsRedstoneCheck() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiSensor> getGuiClass() {
        return GuiSensor.class;
    }

    private static long lastTime = 0;

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block gives a redstone signal");
            list.add(TextFormatting.WHITE + "depending on various circumstances in");
            list.add(TextFormatting.WHITE + "front of it. Like block placement, crop");
            list.add(TextFormatting.WHITE + "growth level, number of entities, ...");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof SensorTileEntity) {
            SensorTileEntity sensor = (SensorTileEntity) te;
            SensorType sensorType = sensor.getSensorType();
            if (sensorType.isSupportsNumber()) {
                probeInfo.text("Type: " + sensorType.getName() + " (" + sensor.getNumber() + ")");
            } else {
                probeInfo.text("Type: " + sensorType.getName());
            }
            int blockCount = sensor.getAreaType().getBlockCount();
            if (blockCount == 1) {
                probeInfo.text("Area: 1 block");
            } else {
                probeInfo.text("Area: " + blockCount + " blocks");
            }
            boolean rc = sensor.checkSensor();
            probeInfo.text(TextFormatting.GREEN + "Output: " + TextFormatting.WHITE + (rc ? "on" : "off"));
        }
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SensorTileEntity) {
            SensorTileEntity sensor = (SensorTileEntity) te;
            sensor.invalidateCache();
        }
        return super.rotateBlock(world, pos, axis);
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        return currenttip;
    }


    @Override
    public int getGuiID() {
        return RFTools.GUI_SENSOR;
    }
}
