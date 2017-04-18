package mcjty.xnet.api.gui;

import mcjty.xnet.api.channels.RSMode;
import net.minecraft.item.ItemStack;

public interface IEditorGui {

    IEditorGui move(int x, int y);

    IEditorGui move(int x);

    IEditorGui shift(int x);

    IEditorGui label(String txt);

    IEditorGui text(String tag, String tooltip, String value, int width);

    IEditorGui integer(String tag, String tooltip, Integer value, int width);

    IEditorGui real(String tag, String tooltip, Double value, int width);

    IEditorGui toggle(String tag, String tooltip, boolean value);

    IEditorGui toggleText(String tag, String tooltip, String text, boolean value);

    IEditorGui colors(String tag, String tooltip, Integer current, Integer... colors);

    IEditorGui choices(String tag, String tooltip, String current, String... values);

    <T extends Enum<T>> IEditorGui choices(String tag, String tooltip, T current, T... values);

    IEditorGui redstoneMode(String tag, RSMode current);

    IEditorGui ghostSlot(String tag, ItemStack slot);

    IEditorGui nl();
}
