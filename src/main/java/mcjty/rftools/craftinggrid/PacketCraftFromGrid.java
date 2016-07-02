package mcjty.rftools.craftinggrid;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.blocks.storage.ModularStorageItemContainer;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.storage.RemoteStorageItemContainer;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCraftFromGrid implements IMessage {

    private BlockPos pos;
    private int count;
    private boolean test;

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            pos = NetworkTools.readPos(buf);
        } else {
            pos = null;
        }
        count = buf.readInt();
        test = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (pos != null) {
            buf.writeBoolean(true);
            NetworkTools.writePos(buf, pos);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeInt(count);
        buf.writeBoolean(test);
    }

    public PacketCraftFromGrid() {
    }

    public PacketCraftFromGrid(BlockPos pos, int count, boolean test) {
        this.pos = pos;
        this.count = count;
        this.test = test;
    }

    public static class Handler implements IMessageHandler<PacketCraftFromGrid, IMessage> {
        @Override
        public IMessage onMessage(PacketCraftFromGrid message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketCraftFromGrid message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            int[] testResult = null;
            if (message.pos == null) {
                // Handle tablet version
                ItemStack mainhand = player.getHeldItemMainhand();
                if (mainhand != null && mainhand.getItem() == ModularStorageSetup.storageModuleTabletItem) {
                    if (player.openContainer instanceof ModularStorageItemContainer) {
                        ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) player.openContainer;
                        testResult = storageItemContainer.getCraftingGridProvider().craft(player, message.count, message.test);
                    } else if (player.openContainer instanceof RemoteStorageItemContainer) {
                        RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) player.openContainer;
                        testResult = storageItemContainer.getCraftingGridProvider().craft(player, message.count, message.test);
                    }
                }
            } else {
                TileEntity te = player.worldObj.getTileEntity(message.pos);
                if (te instanceof CraftingGridProvider) {
                    testResult = ((CraftingGridProvider) te).craft(player, message.count, message.test);
                }
            }
            if (testResult != null) {
                RFToolsMessages.INSTANCE.sendTo(new PacketCraftTestResultToClient(testResult), player);
            }
        }
    }
}
