package mcjty.rftools.commands;

public class CommandRftDb extends DefaultCommand {
    public CommandRftDb() {
        super();
        registerCommand(new CmdDebugToggle());
        registerCommand(new CmdRefreshWorld());
        registerCommand(new CmdListEntities());
        registerCommand(new CmdRCC());
    }

    @Override
    public String getCommandName() {
        return "rftdb";
    }
}
