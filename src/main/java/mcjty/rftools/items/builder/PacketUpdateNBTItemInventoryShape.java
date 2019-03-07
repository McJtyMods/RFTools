package mcjty.rftools.items.builder;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.blocks.builder.BuilderTileEntity;
import mcjty.rftools.blocks.shaper.ComposerTileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketUpdateNBTItemInventoryShape implements IMessage {

    public BlockPos pos;
    public int slotIndex;
    public NBTTagCompound tagCompound;

    public PacketUpdateNBTItemInventoryShape() {
    }

    public PacketUpdateNBTItemInventoryShape(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketUpdateNBTItemInventoryShape(BlockPos pos, int slotIndex, NBTTagCompound tagCompound) {
        this.pos = pos;
        this.slotIndex = slotIndex;
        this.tagCompound = tagCompound;
    }

    protected boolean isValidBlock(World world, BlockPos blockPos, TileEntity tileEntity) {
        return tileEntity instanceof ComposerTileEntity || tileEntity instanceof BuilderTileEntity;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        slotIndex = buf.readInt();
        tagCompound = NetworkTools.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(slotIndex);
        NetworkTools.writeTag(buf, tagCompound);
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = ctx.getSender().getEntityWorld();
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IInventory) {
                if (!isValidBlock(world, pos, te)) {
                    return;
                }
                IInventory inv = (IInventory) te;
                ItemStack stack = inv.getStackInSlot(slotIndex);
                if (!stack.isEmpty()) {
                    stack.setTagCompound(tagCompound);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
