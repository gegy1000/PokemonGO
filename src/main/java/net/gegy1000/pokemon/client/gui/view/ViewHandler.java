package net.gegy1000.pokemon.client.gui.view;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class ViewHandler {
    protected Minecraft mc = Minecraft.getMinecraft();
    protected FontRenderer fontRendererObj = this.mc.fontRendererObj;
    private PokemonViewGUI gui;

    public void setGUI(PokemonViewGUI gui) {
        this.gui = gui;
    }

    protected PokemonViewGUI getGUI() {
        return this.gui;
    }

    protected void drawTexturedModalRect(float x, float y, float minU, float minV, float maxU, float maxV, float width, float height) {
        this.getGUI().drawTexturedModalRect(x, y, minU, minV, maxU, maxV, width, height);
    }

    protected void drawTexturedModalRect(float x, float y, int textureX, int textureY, int width, int height, int texWidth, int texHeight, double scaleX, double scaleY) {
        this.getGUI().drawTexturedModalRect(x, y, textureX, textureY, width, height, texWidth, texHeight, scaleX, scaleY);
    }

    protected void drawRectangle(double x, double y, double width, double height, int color) {
        this.getGUI().drawRectangle(x, y, width, height, color);
    }

    protected void drawHoveringText(List<String> textLines, int x, int y) {
        GuiUtils.drawHoveringText(textLines, x, y, this.getGUI().width, this.getGUI().height, -1, this.fontRendererObj);
    }

    public abstract void render(float mouseX, float mouseY, float partialTicks);

    public abstract void mouseClicked(float mouseX, float mouseY);

    public abstract void initView();

    public abstract void cleanupView();
}
