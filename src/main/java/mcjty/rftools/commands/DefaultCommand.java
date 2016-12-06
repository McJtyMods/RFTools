package mcjty.rftools.commands;

import mcjty.lib.compat.CompatCommand;
import mcjty.lib.compat.CompatCommandBase;
import mcjty.lib.tools.ChatTools;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public abstract class DefaultCommand implements CompatCommand {
    protected final Map<String,RfToolsCommand> commands = new HashMap<String, RfToolsCommand>();

    public DefaultCommand() {
        registerCommand(new CmdHelp());
    }

    protected void registerCommand(RfToolsCommand command) {
        commands.put(command.getCommand(), command);
    }

    public void showHelp(ICommandSender sender) {
        ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.BLUE + getName() + " <subcommand> <args>"));
        for (Map.Entry<String,RfToolsCommand> me : commands.entrySet()) {
            ChatTools.addChatMessage(sender, new TextComponentString("    " + me.getKey() + " " + me.getValue().getHelp()));
        }
    }

    class CmdHelp implements RfToolsCommand {
        @Override
        public String getHelp() {
            return "";
        }

        @Override
        public int getPermissionLevel() {
            return 0;
        }

        @Override
        public boolean isClientSide() {
            return false;
        }

        @Override
        public String getCommand() {
            return "help";
        }

        @Override
        public void execute(ICommandSender sender, String[] args) {
            showHelp(sender);
        }
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return getName() + " <subcommand> <args> (try '" + getName() + " help' for more info)";
    }


    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        World world = sender.getEntityWorld();
        if (args.length <= 0) {
            if (!world.isRemote) {
                showHelp(sender);
            }
        } else {
            RfToolsCommand command = commands.get(args[0]);
            if (command == null) {
                if (!world.isRemote) {
                    ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Unknown RfTools command: " + args[0]));
                }
            } else {
                if (world.isRemote) {
                    // We are client-side. Only do client-side commands.
                    if (command.isClientSide()) {
                        command.execute(sender, args);
                    }
                } else {
                    // Server-side.
                    if (!CompatCommandBase.canUseCommand(sender, command.getPermissionLevel(), getName())) {
                        ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Command is not allowed!"));
                    } else {
                        command.execute(sender, args);
                    }
//                    if (!sender.canCommandSenderUseCommand(command.getPermissionLevel(), getCommandName())) {
//                        sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Command is not allowed!"));
//                    } else {
//                        command.execute(sender, args);
//                    }
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return new ArrayList<>();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public boolean isUsernameIndex(String[] sender, int p_82358_2_) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(CompatCommandBase.getCommandName(o));
    }
}
