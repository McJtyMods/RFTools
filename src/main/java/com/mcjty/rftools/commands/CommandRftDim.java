package com.mcjty.rftools.commands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRftDim implements ICommand {
    private final Map<String,RfToolsCommand> commands = new HashMap<String, RfToolsCommand>();

    public CommandRftDim() {
        registerCommand(new CmdListDimensions());
        registerCommand(new CmdTeleport());
        registerCommand(new CmdHelp());
    }

    private void registerCommand(RfToolsCommand command) {
        commands.put(command.getCommand(), command);
    }

    @Override
    public String getCommandName() {
        return "rftdim";
    }

    public void showHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + "rftdim <subcommand> <args>"));
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
        return "rftdim <subcommand> <args> (try 'rftdim help' for more info)";
    }

    @Override
    public List getCommandAliases() {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        World world = sender.getEntityWorld();
        if (!world.isRemote) {
            if (args.length <= 0) {
                showHelp(sender);
            } else {
                RfToolsCommand command = commands.get(args[0]);
                if (command == null) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown RfTools command: " + args[0]));
                } else {
                    command.execute(sender, args);
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
    public int compareTo(Object o) {
        return 0;
    }
}
