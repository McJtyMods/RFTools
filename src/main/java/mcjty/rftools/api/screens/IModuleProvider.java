package mcjty.rftools.api.screens;

public interface IModuleProvider {

    Class<? extends IScreenModule> getServerScreenModule();

    Class<? extends IClientScreenModule> getClientScreenModule();

    String getName();
}
