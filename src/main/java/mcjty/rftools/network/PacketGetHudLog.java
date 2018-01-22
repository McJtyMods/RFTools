package mcjty.rftools.network;

import mcjty.lib.network.PacketRequestServerList;
import mcjty.lib.network.PacketRequestServerListHandler;
import mcjty.rftools.RFTools;
import mcjty.typed.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import java.util.List;

public class PacketGetHudLog extends PacketRequestServerList<String> {

    public static String CMD_GETHUDLOG = "getHudLog";
    public static String CLIENTCMD_GETHUDLOG = "getHudLog";

    public PacketGetHudLog() {
    }

    public PacketGetHudLog(BlockPos pos) {
        super(RFTools.MODID, pos, CMD_GETHUDLOG);
    }

    public static class Handler extends PacketRequestServerListHandler<PacketGetHudLog, String> {

        public Handler() {
            super(Type.STRING);
        }

        @Override
        protected void sendToClient(BlockPos pos, @Nonnull List<String> list, MessageContext messageContext) {
            RFToolsMessages.INSTANCE.sendTo(new PacketHudLogReady(pos, CLIENTCMD_GETHUDLOG, list), messageContext.getServerHandler().player);
        }
    }
}
