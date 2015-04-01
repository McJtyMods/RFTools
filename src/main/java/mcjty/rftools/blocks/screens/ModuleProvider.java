package mcjty.rftools.blocks.screens;

import mcjty.rftools.blocks.screens.modules.ScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;

public interface ModuleProvider {

    Class<? extends ScreenModule> getServerScreenModule();

    Class<? extends ClientScreenModule> getClientScreenModule();

    String getName();
}
