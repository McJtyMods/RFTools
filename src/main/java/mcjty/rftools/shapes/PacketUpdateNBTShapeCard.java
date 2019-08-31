package mcjty.rftools.shapes;

import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class PacketUpdateNBTShapeCard {
    private TypedMap args;

    public void toBytes(PacketBuffer buf) {
        TypedMapTools.writeArguments(buf, args);
    }

    public PacketUpdateNBTShapeCard() {
    }

    public PacketUpdateNBTShapeCard(PacketBuffer buf) {
        args = TypedMapTools.readArguments(buf);
    }

    public PacketUpdateNBTShapeCard(TypedMap arguments) {
        this.args = arguments;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity playerEntity = ctx.getSender();
            ItemStack heldItem = playerEntity.getHeldItem(Hand.MAIN_HAND);
            if (heldItem.isEmpty()) {
                return;
            }
            CompoundNBT tagCompound = heldItem.getTag();
            if (tagCompound == null) {
                tagCompound = new CompoundNBT();
                heldItem.setTag(tagCompound);
            }
            for (Key<?> akey : args.getKeys()) {
                String key = akey.getName();
                if (Type.STRING.equals(akey.getType())) {
                    tagCompound.putString(key, (String) args.get(akey));
                } else if (Type.INTEGER.equals(akey.getType())) {
                    tagCompound.putInt(key, (Integer) args.get(akey));
                } else if (Type.DOUBLE.equals(akey.getType())) {
                    tagCompound.putDouble(key, (Double) args.get(akey));
                } else if (Type.BOOLEAN.equals(akey.getType())) {
                    tagCompound.putBoolean(key, (Boolean) args.get(akey));
                } else if (Type.BLOCKPOS.equals(akey.getType())) {
                    throw new RuntimeException("BlockPos not supported for PacketUpdateNBTItem!");
                } else if (Type.ITEMSTACK.equals(akey.getType())) {
                    throw new RuntimeException("ItemStack not supported for PacketUpdateNBTItem!");
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}