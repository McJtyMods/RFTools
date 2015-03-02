package com.mcjty.rftools.commands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DefaultCommand implements ICommand {
    protected final Map<String,RfToolsCommand> commands = new HashMap<String, RfToolsCommand>();

    public DefaultCommand() {
        registerCommand(new CmdHelp());
    }

    protected void registerCommand(RfToolsCommand command) {
        commands.put(command.getCommand(), command);
    }

    public void showHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + getCommandName() + " <subcommand> <args>"));
        for (Map.Entry<String,RfToolsCommand> me : commands.entrySet()) {
            sender.addChatMessage(new ChatComponentText("    " + me.getKey() + " " + me.getValue().getHelp()));
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
    public String getCommandUsage(ICommandSender sender) {
        return getCommandName() + " <subcommand> <args> (try '" + getCommandName() + " help' for more info)";
    }

    @Override
    public List getCommandAliases() {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        World world = sender.getEntityWorld();
        if (args.length <= 0) {
            if (!world.isRemote) {
                showHelp(sender);
            }
        } else {
            RfToolsCommand command = commands.get(args[0]);
            if (command == null) {
                if (!world.isRemote) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown RfTools command: " + args[0]));
                }
            } else {
                if (world.isRemote) {
                    // We are client-side. Only do client-side commands.
                    if (command.isClientSide()) {
                        command.execute(sender, args);
                    }
                } else {
                    // Server-side.
                    if (!sender.canCommandSenderUseCommand(command.getPermissionLevel(), getCommandName())) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Command is not allowed!"));
                    } else {
                        command.execute(sender, args);
                    }
                }
            }
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return null;
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
    public int compareTo(Object o) {
        return getCommandName().compareTo(((ICommand)o).getCommandName());
    }
}
