package mcjty.rftools.commands;

public class CommandRftShape extends DefaultCommand {
    public CommandRftShape() {
        super();
        registerCommand(new CmdSaveCard());
        registerCommand(new CmdLoadCard());
    }

    @Override
    public String getName() {
        return "rftshape";
    }
}
