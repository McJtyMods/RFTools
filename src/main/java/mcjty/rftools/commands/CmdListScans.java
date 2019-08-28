package mcjty.rftools.commands;

import mcjty.rftools.shapes.ScanDataManager;
import net.minecraft.entity.player.PlayerEntity;

public class CmdListScans extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "listscans";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(PlayerEntity sender, String[] args) {
        ScanDataManager.listScans(sender);
    }
}
