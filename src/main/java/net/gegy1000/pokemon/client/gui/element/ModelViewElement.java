package net.gegy1000.pokemon.client.gui.element;

import net.ilexiconn.llibrary.client.ClientProxy;
import net.ilexiconn.llibrary.client.gui.element.Element;
import net.ilexiconn.llibrary.client.gui.element.IElementGUI;
import net.ilexiconn.llibrary.client.util.ClientUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector2f;
import java.util.function.Function;

public class ModelViewElement<T extends IElementGUI> extends Element<T> {
    private Function<ModelViewElement, Void> renderFunction;
    private Function<Vector2f, Boolean> clickFunction;

    private int backgroundColour;

    private float zoom;
    private float zoomVelocity;

    private float cameraOffsetX;
    private float cameraOffsetY;

    private float prevRotationPitch;
    private float rotationPitch;

    private float prevRotationYaw;
    private float rotationYaw;

    private float prevMouseX;
    private float prevMouseY;

    private boolean lookMouse;
    private boolean dragged;

    public ModelViewElement(T gui, float posX, float posY, int width, int height, Function<ModelViewElement, Void> renderFunction, Function<Vector2f, Boolean> clickFunction, int backgroundColour, boolean lookMouse) {
        super(gui, posX, posY, width, height);
        this.renderFunction = renderFunction;
        this.clickFunction = clickFunction;
        this.lookMouse = lookMouse;
        this.backgroundColour = backgroundColour;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        this.startScissor();
        ScaledResolution resolution = new ScaledResolution(ClientProxy.MINECRAFT);
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableNormalize();
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GLU.gluPerspective(30.0F, (float) (resolution.getScaledWidth_double() / resolution.getScaledHeight_double()), 1.0F, 10000.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.clearColor((float) (this.backgroundColour >> 16 & 0xFF) / 255.0F, (float) (this.backgroundColour >> 8 & 0xFF) / 255.0F, (float) (this.backgroundColour & 0xFF) / 255.0F, 1.0F);
        GlStateManager.enableLighting();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.translate((this.getPosX() * 0.15) / resolution.getScaleFactor(), (this.getPosY() * 0.15) / resolution.getScaleFactor(), 0.0F);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
        GlStateManager.disableTexture2D();
        GlStateManager.translate(0.0F, -2.0F, -10.0F);
        GlStateManager.scale(this.zoom, this.zoom, this.zoom);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.translate(this.cameraOffsetX, this.cameraOffsetY, 0.0F);
        GlStateManager.rotate(ClientUtils.interpolate(this.prevRotationPitch, this.rotationPitch, partialTicks), 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(ClientUtils.interpolate(this.prevRotationYaw, this.rotationYaw, partialTicks), 0.0F, 1.0F, 0.0F);
        this.renderFunction.apply(this);
        GlStateManager.enableTexture2D();
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0.0, -5000.0D, 5000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.disableBlend();
        this.endScissor();
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        this.zoom += this.zoomVelocity;
        this.zoomVelocity *= 0.6F;
        if (this.zoom < 0.5F) {
            this.zoom = 0.5F;
        } else if (this.zoom > 10.0F) {
            this.zoom = 10.0F;
        }
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (this.isSelected(mouseX, mouseY)) {
            this.prevMouseX = mouseX;
            this.prevMouseY = mouseY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, int button, long timeSinceClick) {
        if (this.lookMouse) {
            if (this.isSelected(mouseX, mouseY)) {
                float xMovement = mouseX - this.prevMouseX;
                float yMovement = mouseY - this.prevMouseY;
                if (button == 0) {
                    this.rotationYaw += xMovement / this.zoom;
                    if ((this.rotationPitch > -90.0F || yMovement < 0.0F) && (this.rotationPitch < 90.0F || yMovement > 0.0F)) {
                        this.rotationPitch -= yMovement / this.zoom;
                    }
                    this.dragged = true;
                    this.prevMouseX = mouseX;
                    this.prevMouseY = mouseY;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        if (this.isSelected(mouseX, mouseY)) {
            if (this.dragged) {
                this.dragged = false;
            } else {
                return this.clickFunction.apply(new Vector2f(mouseX, mouseY));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(float mouseX, float mouseY, int amount) {
        if (this.lookMouse) {
            this.zoomVelocity += (amount / 120.0F) * 0.05F;
        }
        return true;
    }

    public ModelViewElement<T> withCameraOffset(float x, float y) {
        this.cameraOffsetX = x;
        this.cameraOffsetY = y;
        return this;
    }

    public ModelViewElement<T> withRotation(float yaw, float pitch) {
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
        return this;
    }

    public ModelViewElement<T> withZoom(float zoom) {
        this.zoom = zoom;
        return this;
    }
}
