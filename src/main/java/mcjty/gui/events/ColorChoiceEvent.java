package mcjty.gui.events;

import mcjty.gui.widgets.Widget;

public interface ColorChoiceEvent {
    void choiceChanged(Widget parent, Integer newColor);
}
