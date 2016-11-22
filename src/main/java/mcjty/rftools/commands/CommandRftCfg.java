package mcjty.rftools.commands;

public class CommandRftCfg extends DefaultCommand {
    public CommandRftCfg() {
        super();
        registerCommand(new CmdSetBuffBar());
        registerCommand(new CmdSetStyle());
        registerCommand(new CmdReset());
    }

    @Override
    public String getName() {
        return "rftcfg";
    }
}
