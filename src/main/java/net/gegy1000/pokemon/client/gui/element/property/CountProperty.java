package net.gegy1000.pokemon.client.gui.element.property;

import net.ilexiconn.llibrary.server.property.IFloatRangeProperty;
import net.ilexiconn.llibrary.server.property.IStringProperty;

import java.util.function.Consumer;

public class CountProperty implements IFloatRangeProperty, IStringProperty {
    private final Consumer<Integer> submit;
    private final int min;
    private final int max;

    private int value;

    public CountProperty(Consumer<Integer> submit, int min, int max, int value) {
        this.submit = submit;
        this.min = min;
        this.max = max;
        this.value = value;
    }

    @Override
    public float getMinFloatValue() {
        return this.min;
    }

    @Override
    public float getMaxFloatValue() {
        return this.max;
    }

    @Override
    public float getFloat() {
        return this.value;
    }

    @Override
    public void setFloat(float value) {
        this.value = (int) value;
        this.submit.accept(this.value);
    }

    @Override
    public String getString() {
        return String.valueOf(this.value);
    }

    @Override
    public void setString(String text) {
        this.value = Integer.parseInt(text);
    }

    @Override
    public boolean isValidString(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void set(int value) {
        this.value = value;
    }
}
