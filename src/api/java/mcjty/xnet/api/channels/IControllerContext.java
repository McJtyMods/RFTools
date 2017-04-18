package mcjty.xnet.api.channels;

import mcjty.xnet.api.keys.ConsumerId;
import mcjty.xnet.api.keys.NetworkId;
import mcjty.xnet.api.keys.SidedConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface IControllerContext {

    World getControllerWorld();

    NetworkId getNetworkId();

    @Nullable
    BlockPos findConsumerPosition(@Nonnull ConsumerId consumerId);

    @Nonnull
    Map<SidedConsumer, IConnectorSettings> getConnectors(int channel);

    @Nonnull
    Map<SidedConsumer, IConnectorSettings> getRoutedConnectors(int channel);

    boolean matchColor(int colorMask);

    boolean checkAndConsumeRF(int rft);

}
