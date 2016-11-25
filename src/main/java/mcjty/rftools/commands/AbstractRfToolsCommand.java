package mcjty.rftools.commands;

import mcjty.lib.tools.ChatTools;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public abstract class AbstractRfToolsCommand implements RfToolsCommand {

    protected String fetchString(ICommandSender sender, String[] args, int index, String defaultValue) {
        try {
            return args[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return defaultValue;
        }
    }

    protected boolean fetchBool(ICommandSender sender, String[] args, int index, boolean defaultValue) {
        boolean value;
        try {
            value = Boolean.valueOf(args[index]);
        } catch (NumberFormatException e) {
            value = false;
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Parameter is not a valid boolean!"));
        } catch (ArrayIndexOutOfBoundsException e) {
            return defaultValue;
        }
        return value;
    }

    protected int fetchInt(ICommandSender sender, String[] args, int index, int defaultValue) {
        int value;
        try {
            value = Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            value = 0;
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Parameter is not a valid integer!"));
        } catch (ArrayIndexOutOfBoundsException e) {
            return defaultValue;
        }
        return value;
    }

    protected float fetchFloat(ICommandSender sender, String[] args, int index, float defaultValue) {
        float value;
        try {
            value = Float.parseFloat(args[index]);
        } catch (NumberFormatException e) {
            value = 0.0f;
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Parameter is not a valid real number!"));
        } catch (ArrayIndexOutOfBoundsException e) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }
}
