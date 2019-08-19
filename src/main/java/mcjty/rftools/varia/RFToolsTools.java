package mcjty.rftools.varia;

import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.varia.EnergyTools;
import mcjty.rftools.network.MachineInfo;
import mcjty.rftools.network.PacketReturnRfInRange;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class RFToolsTools {

    public static boolean hasModuleTarget(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return false;
        }
        return stack.getTag().hasKey("monitorx");
    }

    public static int getDimensionFromModule(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 0;
        }
        return stack.getTag().getInteger("monitordim");
    }

    public static void setPositionInModule(ItemStack stack, Integer dimension, BlockPos pos, String name) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new CompoundNBT());
        }
        if (dimension != null) {
            stack.getTag().setInteger("monitordim", dimension);
        }
        if (name != null) {
            stack.getTag().setString("monitorname", name);
        }
        stack.getTag().setInteger("monitorx", pos.getX());
        stack.getTag().setInteger("monitory", pos.getY());
        stack.getTag().setInteger("monitorz", pos.getZ());
    }

    public static void clearPositionInModule(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new CompoundNBT());
        }
        CompoundNBT tagCompound = stack.getTag();
        tagCompound.removeTag("monitordim");
        tagCompound.removeTag("monitorx");
        tagCompound.removeTag("monitory");
        tagCompound.removeTag("monitorz");
        tagCompound.removeTag("monitorname");
    }

    public static BlockPos getPositionFromModule(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return null;
        }
        CompoundNBT tagCompound = stack.getTag();
        int monitorx = tagCompound.getInteger("monitorx");
        int monitory = tagCompound.getInteger("monitory");
        int monitorz = tagCompound.getInteger("monitorz");
        return new BlockPos(monitorx, monitory, monitorz);
    }

    public static void returnRfInRange(PlayerEntity player) {
        BlockPos pos = player.getPosition();
        World world = player.getEntityWorld();
        Map<BlockPos, MachineInfo> result = new HashMap<>();
        int range = 12;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos p = pos.add(x, y, z);
                    TileEntity te = world.getTileEntity(p);
                    if (EnergyTools.isEnergyTE(te, null)) {
                        EnergyTools.EnergyLevel level = EnergyTools.getEnergyLevel(te, null);
                        Long usage = null;
                        if (te instanceof IMachineInformation) {
                            usage = ((IMachineInformation) te).getEnergyDiffPerTick();
                        }
                        result.put(p, new MachineInfo(level.getEnergy(), level.getMaxEnergy(), usage));
                    }
                }
            }
        }

        RFToolsMessages.INSTANCE.sendTo(new PacketReturnRfInRange(result), (EntityPlayerMP) player);
    }
}
