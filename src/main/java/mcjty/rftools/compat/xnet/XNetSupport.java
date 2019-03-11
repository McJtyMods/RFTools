package mcjty.rftools.compat.xnet;

import com.google.common.base.Function;
import mcjty.xnet.api.IXNet;

import javax.annotation.Nullable;

public class XNetSupport {

    public static IXNet xnet;

    public static class GetXNet implements Function<IXNet, Void> {
        @Nullable
        @Override
        public Void apply(IXNet input) {
            xnet = input;
            xnet.registerChannelType(new StorageChannelType());
            return null;
        }
    }
}
