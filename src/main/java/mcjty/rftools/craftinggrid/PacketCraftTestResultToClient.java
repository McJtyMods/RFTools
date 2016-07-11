package mcjty.rftools.craftinggrid;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.RFTools;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCraftTestResultToClient implements IMessage {

    private int[] testResult;

    @Override
    public void fromBytes(ByteBuf buf) {
        testResult = new int[10];
        for (int i = 0 ; i < 10 ; i++) {
            testResult[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (int i = 0 ; i < 10 ; i++) {
            buf.writeInt(testResult[i]);
        }
    }

    public PacketCraftTestResultToClient() {
    }

    public PacketCraftTestResultToClient(int[] testResult) {
        this.testResult = testResult;
    }

    public static class Handler implements IMessageHandler<PacketCraftTestResultToClient, IMessage> {
        @Override
        public IMessage onMessage(PacketCraftTestResultToClient message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketCraftTestResultToClient message, MessageContext ctx) {
            GuiCraftingGrid.testResultFromServer = message.testResult;
        }
    }
}
