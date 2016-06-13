package mcjty.rftools.blocks.spawner;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;

public class SpawnerInfoPacketClient implements InfoPacketClient {

    private float[] matter;

    public static float matterReceived[] = null;

    public SpawnerInfoPacketClient() {
    }

    public SpawnerInfoPacketClient(float[] matter) {
        this.matter = matter;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            matter = new float[] {
                buf.readFloat(), buf.readFloat(), buf.readFloat()
            };
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (matter == null || matter.length < 3) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeFloat(matter[0]);
            buf.writeFloat(matter[1]);
            buf.writeFloat(matter[2]);
        }
    }

    @Override
    public void onMessageClient(EntityPlayerSP player) {
        matterReceived = matter;
    }
}
