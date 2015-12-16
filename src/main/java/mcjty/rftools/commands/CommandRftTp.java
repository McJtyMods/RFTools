package mcjty.rftools.commands;

public class CommandRftTp extends DefaultCommand {
    public CommandRftTp() {
        super();
//        registerCommand(new CmdListReceivers());
//        registerCommand(new CmdCleanupReceivers());
    }

    @Override
    public String getCommandName() {
        return "rfttp";
    }
}
