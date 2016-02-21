package mcjty.rftools.api.screens;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.api.screens.data.IModuleDataBoolean;
import mcjty.rftools.api.screens.data.IModuleDataInteger;
import mcjty.rftools.api.screens.data.IModuleDataString;

/**
 * Helper to create IScreenData instances for simple and common objects
 */
public interface IScreenDataHelper {

    IModuleDataInteger createInteger(int i);
    IModuleDataInteger createInteger(ByteBuf buf);

    IModuleDataBoolean createBoolean(boolean b);
    IModuleDataBoolean createBoolean(ByteBuf buf);

    IModuleDataString createString(String b);
    IModuleDataString createString(ByteBuf buf);
}
