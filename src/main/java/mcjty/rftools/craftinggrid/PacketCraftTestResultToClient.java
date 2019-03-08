package mcjty.rftools.craftinggrid;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketCraftTestResultToClient implements IMessage {

    private int[] testResult;

    @Override
    public void fromBytes(ByteBuf buf) {
        testResult = new int[10];
        for (int i = 0; i < 10; i++) {
            testResult[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (int i = 0; i < 10; i++) {
            buf.writeInt(testResult[i]);
        }
    }

    public PacketCraftTestResultToClient() {
    }

    public PacketCraftTestResultToClient(ByteBuf buf) {
    }

    public PacketCraftTestResultToClient(int[] testResult) {
        this.testResult = testResult;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiCraftingGrid.testResultFromServer = testResult;

        });
        ctx.setPacketHandled(true);
    }

}