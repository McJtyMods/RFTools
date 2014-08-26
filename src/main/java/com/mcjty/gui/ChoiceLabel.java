package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.ArrayList;

public class ChoiceLabel extends Label<ChoiceLabel> {
    private ArrayList<String> choiceList = new ArrayList<String>();
    private String currentChoice = null;

    public ChoiceLabel(Minecraft mc, Gui gui) {
        super(mc, gui);
        setText("");
    }

    public ChoiceLabel addChoices(String ... choices) {
        for (String choice : choices) {
            choiceList.add(choice);
            if (currentChoice == null) {
                currentChoice = choice;
                setText(currentChoice);
            }
        }
        return this;
    }

    public ChoiceLabel setChoice(String choice) {
        currentChoice = choice;
        setText(currentChoice);
        return this;
    }

    @Override
    public void draw(int x, int y) {
        int xx = x + bounds.x;
        int yy = y + bounds.y;

        RenderHelper.drawBeveledBox(xx, yy, xx + bounds.width-1, yy+bounds.height-1, 0xffeeeeee, 0xff333333, 0xff666666);
//        Gui.drawRect(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, 0xff555555);

        super.draw(x, y);
    }

    @Override
    public Widget mouseClick(int x, int y, int button) {
        int index = choiceList.indexOf(currentChoice);
        index++;
        if (index >= choiceList.size()) {
            index = 0;
        }
        currentChoice = choiceList.get(index);
        setText(currentChoice);
        return null;
    }
}
