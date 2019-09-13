package mcjty.rftools.varia;

import mcjty.lib.api.information.IPowerInformation;
import mcjty.lib.varia.EnergyTools;
import mcjty.rftools.network.MachineInfo;
import mcjty.rftools.network.PacketReturnRfInRange;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.HashMap;
import java.util.Map;

public class RFToolsTools {

    public static boolean hasModuleTarget(ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        return stack.getTag().contains("monitorx");
    }

    public static int getDimensionFromModule(ItemStack stack) {
        if (!stack.hasTag()) {
            return 0;
        }
        return stack.getTag().getInt("monitordim");
    }

    public static void setPositionInModule(ItemStack stack, Integer dimension, BlockPos pos, String name) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (dimension != null) {
            tag.putInt("monitordim", dimension);
        }
        if (name != null) {
            tag.putString("monitorname", name);
        }
        tag.putInt("monitorx", pos.getX());
        tag.putInt("monitory", pos.getY());
        tag.putInt("monitorz", pos.getZ());
    }

    public static void clearPositionInModule(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        tag.remove("monitordim");
        tag.remove("monitorx");
        tag.remove("monitory");
        tag.remove("monitorz");
        tag.remove("monitorname");
    }

    public static BlockPos getPositionFromModule(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        int monitorx = tag.getInt("monitorx");
        int monitory = tag.getInt("monitory");
        int monitorz = tag.getInt("monitorz");
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
                        if (te instanceof IPowerInformation) {
                            usage = ((IPowerInformation) te).getEnergyDiffPerTick();
                        }
                        result.put(p, new MachineInfo(level.getEnergy(), level.getMaxEnergy(), usage));
                    }
                }
            }
        }

        RFToolsMessages.INSTANCE.sendTo(new PacketReturnRfInRange(result), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }
}
