package net.gegy1000.pokemon.client.gui.element;

import net.ilexiconn.llibrary.client.gui.element.Element;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector2f;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class DrawableElement<T extends GuiScreen> extends Element<T> {
    private Function<Vector2f, Void> renderFunction;

    public DrawableElement(T gui, float posX, float posY, int width, int height, Function<Vector2f, Void> renderFunction) {
        super(gui, posX, posY, width, height);
        this.renderFunction = renderFunction;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.getPosX(), this.getPosY(), 0.0);
        this.renderFunction.apply(new Vector2f(mouseX, mouseY));
        GlStateManager.popMatrix();
    }
}
