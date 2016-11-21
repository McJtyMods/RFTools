package mcjty.rftools.commands;

import mcjty.lib.tools.ChatTools;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public abstract class DefaultCommand implements ICommand {
    protected final Map<String,RfToolsCommand> commands = new HashMap<String, RfToolsCommand>();

    public DefaultCommand() {
        registerCommand(new CmdHelp());
    }

    protected void registerCommand(RfToolsCommand command) {
        commands.put(command.getCommand(), command);
    }

    public void showHelp(ICommandSender sender) {
        // @todo @@@@@@@@@@@@@@@@@@@@@@@@@@@ getCommandName()? getName()?
        ChatTools.addChatMessage((EntityPlayer) sender, new TextComponentString(TextFormatting.BLUE + getName() + " <subcommand> <args>"));
        for (Map.Entry<String,RfToolsCommand> me : commands.entrySet()) {
            ChatTools.addChatMessage((EntityPlayer) sender, new TextComponentString("    " + me.getKey() + " " + me.getValue().getHelp()));
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

    // @todo @@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    @Override
    public String getUsage(ICommandSender sender) {
        return getName() + " <subcommand> <args> (try '" + getName() + " help' for more info)";
    }
//    @Override
//    public String getCommandUsage(ICommandSender sender) {
//        return getCommandName() + " <subcommand> <args> (try '" + getCommandName() + " help' for more info)";
//    }


    // @todo @@@@@@@@@@@
    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }
//    @Override
//    public List getCommandAliases() {
//        return Collections.emptyList();
//    }


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
                    ChatTools.addChatMessage((EntityPlayer) sender, new TextComponentString(TextFormatting.RED + "Unknown RfTools command: " + args[0]));
                }
            } else {
                if (world.isRemote) {
                    // We are client-side. Only do client-side commands.
                    if (command.isClientSide()) {
                        command.execute(sender, args);
                    }
                } else {
                    // Server-side.
                    // @todo @@@@@@@@@@@@@@@@
                    if (!sender.canUseCommand(command.getPermissionLevel(), getName())) {
                        ChatTools.addChatMessage((EntityPlayer) sender, new TextComponentString(TextFormatting.RED + "Command is not allowed!"));
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

    // @todo @@@@@@@@
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return new ArrayList<>();
    }
//    @Override
//    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
//        return new ArrayList<>();
//    }

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

    // @todo @@@@@@@@@@@@@
    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(((ICommand)o).getName());
    }
//    @Override
//    public int compareTo(ICommand o) {
//        return getCommandName().compareTo(((ICommand)o).getCommandName());
//    }
}
