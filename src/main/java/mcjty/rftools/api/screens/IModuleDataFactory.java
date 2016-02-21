package mcjty.rftools.api.screens;

import io.netty.buffer.ByteBuf;

/**
 * A factory for IModuleData
 */
public interface IModuleDataFactory<T extends IModuleData> {

    T createData(ByteBuf buf);
}
