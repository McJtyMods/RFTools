package com.mcjty.rftools.network;

import io.netty.buffer.ByteBuf;

public interface ByteBufConverter {
    void toBytes(ByteBuf buf);
}
