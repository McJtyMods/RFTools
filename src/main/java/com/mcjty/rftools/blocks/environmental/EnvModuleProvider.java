package com.mcjty.rftools.blocks.environmental;

import com.mcjty.rftools.blocks.environmental.modules.EnvironmentModule;

public interface EnvModuleProvider {
    Class<? extends EnvironmentModule> getServerEnvironmentModule();

    String getName();
}
