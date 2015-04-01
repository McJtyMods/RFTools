package mcjty.gui.events;

import mcjty.gui.widgets.Widget;

public interface SelectionEvent {
    void select(Widget parent, int index);

    void doubleClick(Widget parent, int index);
}
