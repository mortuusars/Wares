package io.github.mortuusars.wares.client.gui.agreement;

import io.github.mortuusars.mpfui.component.Rectangle;
import it.unimi.dsi.fastutil.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AgreementLayout {

    public enum Element {
        TITLE, MESSAGE, SLOTS, ORDERED, EXPIRY;
    }

    private List<Pair<Element, Rectangle>> elements = new ArrayList<>();

    public @Nullable Rectangle getElement(Element type) {
        for (Pair<Element, Rectangle> element : elements) {
            if (element.left() == type)
                return element.right();
        }

        return null;
    }

    public AgreementLayout append(Element type, int xPos, int width, int height, int spacing) {
        if (getElement(type) != null)
            throw new IllegalStateException("Attempted to add same type of element twice.");

        int lastX = elements.size() > 0 ? elements.get(elements.size() - 1).right().bottom() : 0;
        elements.add(Pair.of(type, new Rectangle(xPos, lastX + spacing, width, height)));
        return this;
    }

    public AgreementLayout clear() {
        elements.clear();
        return this;
    }

    public int getHeight() {
        if (elements.size() == 0)
            return 0;

        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;

        for (Pair<Element, Rectangle> element : elements) {
            yMin = Math.min(yMin, element.right().top());
            yMax = Math.max(yMax, element.right().bottom());
        }

        return yMax - yMin;
    }

    public AgreementLayout offset(int x, int y) {
        List<Pair<Element, Rectangle>> modifiedElements = new ArrayList<>();

        for (Pair<Element, Rectangle> element : elements) {
            modifiedElements.add(Pair.of(element.left(), element.right().shift(x, y)));
        }

        AgreementLayout layout = new AgreementLayout();
        layout.elements = modifiedElements;
        return layout;
    }
}
