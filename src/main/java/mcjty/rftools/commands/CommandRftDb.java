package mcjty.rftools.commands;

public class CommandRftDb extends DefaultCommand {
    public CommandRftDb() {
        super();
        registerCommand(new CmdDebugToggle());
//        registerCommand(new CmdListEntities());
    }

    @Override
    public String getCommandName() {
        return "rftdb";
    }
}
