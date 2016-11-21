package mcjty.rftools.commands;

public class CommandRftTp extends DefaultCommand {
    public CommandRftTp() {
        super();
        registerCommand(new CmdListReceivers());
        registerCommand(new CmdTeleport());
//        registerCommand(new CmdCleanupReceivers());
    }

    // @todo @@@@@@@@@@@@@@@
    @Override
    public String getName() {
        return "rfttp";
    }
//    @Override
//    public String getCommandName() {
//        return "rfttp";
//    }
}
