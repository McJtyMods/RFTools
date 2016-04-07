package mcjty.rftools.blocks.environmental;

import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;

public interface EnvModuleProvider {
    Class<? extends EnvironmentModule> getServerEnvironmentModule();

    String getName();
}
