package com.mcjty.rftools.commands;

import com.mcjty.rftools.dimension.world.WorldRefresher;
import net.minecraft.command.ICommandSender;

public class CmdRefreshWorld extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "refresh";
    }

    @Override
    public int getPermissionLevel() {
        return 3;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        WorldRefresher.refreshChunks(sender.getEntityWorld());
    }
}
