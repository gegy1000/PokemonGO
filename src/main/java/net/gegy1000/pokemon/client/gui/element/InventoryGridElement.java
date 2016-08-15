package net.gegy1000.pokemon.client.gui.element;

import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.Element;
import net.ilexiconn.llibrary.client.gui.element.ScrollbarElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class InventoryGridElement<T extends GuiScreen> extends Element<T> {
    private ScrollbarElement<T> scrollbar;
    private final Function<SlotRenderer, Void> renderFunction;
    private final int tileSize;
    private final int tilesX;
    private int size;

    public InventoryGridElement(T gui, float posX, float posY, int width, int height, int tilesX, int tileSize, Function<SlotRenderer, Void> renderFunction) {
        super(gui, posX, posY, width, height);
        this.tilesX = tilesX;
        this.tileSize = tileSize;
        this.renderFunction = renderFunction;
    }

    @Override
    public void init() {
        this.scrollbar = new ScrollbarElement<>(this, () -> (float) this.getWidth(), () -> 2.0F, () -> this.getHeight() - 3.0F, this.tileSize, () -> (int) Math.ceil((float) this.size / this.tilesX));
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -this.scrollbar.getScrollOffset(), 0.0F);
        GlStateManager.translate(this.getPosX(), this.getPosY(), 0.0F);
        mouseX -= this.getPosX();
        mouseY -= this.getPosY();
        mouseY += this.scrollbar.getScrollOffset();
        this.renderFunction.apply(new SlotRenderer(this, mouseX, mouseY));
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    public int getRenderTileSize() {
        return this.tileSize - 2;
    }

    public static class SlotRenderer {
        private final InventoryGridElement<?> grid;
        private final float mouseX;
        private final float mouseY;

        public SlotRenderer(InventoryGridElement<?> grid, float mouseX, float mouseY) {
            this.grid = grid;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        public void draw(Function<Slot, Void> slot, Function<Slot, List<String>> hovering, int amount) {
            this.grid.startScissor();
            this.grid.size = amount;
            int x = 0;
            int y = 0;
            int tileSize = this.grid.tileSize;
            int renderTileSize = this.grid.getRenderTileSize();
            for (int i = 0; i < amount; i++) {
                float renderX = x * tileSize;
                float renderY = y * tileSize;
                this.grid.drawRectangle(renderX, renderY, renderTileSize, renderTileSize, LLibrary.CONFIG.getSecondaryColor());
                slot.apply(new Slot(renderX, renderY, i));
                if (++x >= this.grid.tilesX) {
                    x = 0;
                    y++;
                }
            }
            this.grid.endScissor();
            float scrollOffset = this.grid.scrollbar.getScrollOffset();
            if (this.mouseX >= 0.0F && this.mouseX <= this.grid.getWidth() && this.mouseY >= scrollOffset && this.mouseY <= this.grid.getHeight() + scrollOffset) {
                x = 0;
                y = 0;
                for (int i = 0; i < amount; i++) {
                    float renderX = x * tileSize;
                    float renderY = y * tileSize;
                    if (this.mouseX >= renderX && this.mouseX <= renderX + renderTileSize && this.mouseY >= renderY && this.mouseY <= renderY + renderTileSize) {
                        GuiUtils.drawHoveringText(hovering.apply(new Slot(renderX, renderY, i)), (int) this.mouseX, (int) this.mouseY, (int) (this.grid.getGUI().width - this.grid.getPosX()), (int) (this.grid.getGUI().height + scrollOffset), -1, Minecraft.getMinecraft().fontRendererObj);
                        break;
                    }
                    if (++x >= this.grid.tilesX) {
                        x = 0;
                        y++;
                    }
                }
            }
        }

        public InventoryGridElement<?> getGrid() {
            return this.grid;
        }

        public float getMouseX() {
            return this.mouseX;
        }

        public float getMouseY() {
            return this.mouseY;
        }
    }

    public static class Slot {
        private final float x;
        private final float y;
        private final int index;

        public Slot(float x, float y, int index) {
            this.x = x;
            this.y = y;
            this.index = index;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public int getIndex() {
            return this.index;
        }
    }
}
