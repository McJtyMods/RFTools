package mcjty.rftools.commands;

import mcjty.rftools.RFTools;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CmdListEntities extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "listent";
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
    public void execute(ICommandSender sender, String[] args) {
        if (args.length > 1) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        Set<Map.Entry> set = EntityList.classToStringMapping.entrySet();
        for (Map.Entry entry : set) {
            Class clazz = (Class) entry.getKey();
            String name = (String) entry.getValue();
            Integer id = (Integer) EntityList.classToIDMapping.get(clazz);

            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.US);
            formatter.format("Id:%1$-9d Name:%2$-50.50s Class:%3$-50.50s", id, name, clazz);
            RFTools.log(sb.toString());
        }
    }
}
