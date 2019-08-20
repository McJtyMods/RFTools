package mcjty.rftools.compat.jei;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.blocks.storage.ModularStorageItemContainer;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.storage.RemoteStorageItemContainer;
import mcjty.rftools.blocks.storagemonitor.StorageScannerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketSendRecipe implements IMessage {
    private ItemStackList stacks;
    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        int l = buf.readInt();
        stacks = ItemStackList.create(l);
        for (int i = 0 ; i < l ; i++) {
            if (buf.readBoolean()) {
                stacks.set(i, NetworkTools.readItemStack(buf));
            } else {
                stacks.set(i, ItemStack.EMPTY);
            }
        }
        if (buf.readBoolean()) {
            pos = NetworkTools.readPos(buf);
        } else {
            pos = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(stacks.size());
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            } else {
                buf.writeBoolean(false);
            }
        }
        if (pos != null) {
            buf.writeBoolean(true);
            NetworkTools.writePos(buf, pos);
        } else {
            buf.writeBoolean(false);
        }
    }

    public PacketSendRecipe() {
    }

    public PacketSendRecipe(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketSendRecipe(ItemStackList stacks, BlockPos pos) {
        this.stacks = stacks;
        this.pos = pos;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP player = ctx.getSender();
            World world = player.getEntityWorld();
            if (pos == null) {
                // Handle tablet version
                ItemStack mainhand = player.getHeldItemMainhand();
                if (!mainhand.isEmpty() && mainhand.getItem() == ModularStorageSetup.storageModuleTabletItem) {
                    if (player.openContainer instanceof ModularStorageItemContainer) {
                        ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) player.openContainer;
                        storageItemContainer.getJEIRecipeAcceptor().setGridContents(stacks);
                    } else if (player.openContainer instanceof RemoteStorageItemContainer) {
                        RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) player.openContainer;
                        storageItemContainer.getJEIRecipeAcceptor().setGridContents(stacks);
                    } else if (player.openContainer instanceof StorageScannerContainer) {
                        StorageScannerContainer storageItemContainer = (StorageScannerContainer) player.openContainer;
                        storageItemContainer.getStorageScannerTileEntity().setGridContents(stacks);
                    }
                }
            } else {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof JEIRecipeAcceptor) {
                    JEIRecipeAcceptor acceptor = (JEIRecipeAcceptor) te;
                    acceptor.setGridContents(stacks);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}