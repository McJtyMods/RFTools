package mcjty.rftools.commands;

import mcjty.lib.McJtyLib;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CmdReset extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "reset";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(PlayerEntity sender, String[] args) {
        if (args.length > 1) {
            ITextComponent component = new StringTextComponent(TextFormatting.RED + "Too many parameters!");
            if (sender instanceof PlayerEntity) {
                sender.sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }

        if (!(sender instanceof PlayerEntity)) {
            ITextComponent component = new StringTextComponent(TextFormatting.RED + "This command only works as a player!");
            if (sender instanceof PlayerEntity) {
                sender.sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }

        PlayerEntity player = sender;
        McJtyLib.getPreferencesProperties(player).ifPresent( h -> {
            h.reset();
        });
    }
}
