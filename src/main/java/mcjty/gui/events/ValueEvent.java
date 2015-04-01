package mcjty.gui.events;

import mcjty.gui.widgets.Widget;

public interface ValueEvent {
    void valueChanged(Widget parent, int newValue);
}
