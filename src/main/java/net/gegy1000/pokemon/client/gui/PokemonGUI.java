package net.gegy1000.pokemon.client.gui;

import POGOProtos.Enums.TeamColorOuterClass;
import net.gegy1000.pokemon.PokemonGO;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.ElementGUI;
import net.ilexiconn.llibrary.client.gui.element.color.ColorScheme;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class PokemonGUI extends ElementGUI {
    public static final ColorScheme THEME_TAB_ACTIVE = ColorScheme.create(() -> LLibrary.CONFIG.getAccentColor(), () -> LLibrary.CONFIG.getAccentColor());
    public static final ColorScheme THEME_WINDOW = ColorScheme.create(() -> LLibrary.CONFIG.getSecondaryColor(), () -> LLibrary.CONFIG.getAccentColor());
    public static final ColorScheme THEME_DISABLED = ColorScheme.create(() -> LLibrary.CONFIG.getSecondaryColor(), () -> LLibrary.CONFIG.getSecondaryColor());

    public static final ResourceLocation NEUTRAL_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/neutral.png");
    public static final ResourceLocation INSTINCT_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/instinct.png");
    public static final ResourceLocation VALOR_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/valor.png");
    public static final ResourceLocation MYSTIC_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/mystic.png");
    public static final ResourceLocation STARDUST_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/stardust.png");
    public static final ResourceLocation POKECOIN_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/pokecoin.png");
    public static final ResourceLocation EGG_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/egg.png");

    public void drawTexturedModalRect(float x, float y, float minU, float minV, float maxU, float maxV, float width, float height) {
        GlStateManager.pushMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(minU, maxV).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex(maxU, maxV).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex(maxU, minV).endVertex();
        buffer.pos(x, y, this.zLevel).tex(minU, minV).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    public void drawTexturedModalRect(float x, float y, int textureX, int textureY, int width, int height, int texWidth, int texHeight, double scaleX, double scaleY) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scaleX, scaleY, 0.0);
        x /= scaleX;
        y /= scaleY;
        float f = 1.0F / texWidth;
        float f1 = 1.0F / texHeight;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(textureX * f, (textureY + height) * f1).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex((textureX + width) * f, (textureY + height) * f1).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex((textureX + width) * f, (textureY * f1)).endVertex();
        buffer.pos(x, y, this.zLevel).tex(textureX * f, textureY * f1).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    public void drawRectangle(double x, double y, double width, double height, int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        float a = (float) (color >> 24 & 0xFF) / 255.0F;
        float r = (float) (color >> 16 & 0xFF) / 255.0F;
        float g = (float) (color >> 8 & 0xFF) / 255.0F;
        float b = (float) (color & 0xFF) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexBuffer = tessellator.getBuffer();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.pos(x, y + height, 0.0).color(r, g, b, a).endVertex();
        vertexBuffer.pos(x + width, y + height, 0.0).color(r, g, b, a).endVertex();
        vertexBuffer.pos(x + width, y, 0.0).color(r, g, b, a).endVertex();
        vertexBuffer.pos(x, y, 0.0).color(r, g, b, a).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static class Team {
        private TeamColorOuterClass.TeamColor teamColor;
        private ResourceLocation teamTexture;
        private String teamName;

        public Team(TeamColorOuterClass.TeamColor team) {
            this.teamColor = team;
            this.teamTexture = NEUTRAL_TEXTURE;
            String teamName = "neutral";
            switch (team) {
                case YELLOW:
                    this.teamTexture = INSTINCT_TEXTURE;
                    teamName = "instinct";
                    break;
                case RED:
                    this.teamTexture = VALOR_TEXTURE;
                    teamName = "valor";
                    break;
                case BLUE:
                    this.teamTexture = MYSTIC_TEXTURE;
                    teamName = "mystic";
                    break;
            }

            this.teamName = "team." + teamName + ".name";
        }

        public ResourceLocation getTeamTexture() {
            return this.teamTexture;
        }

        public String getTeamName() {
            return I18n.translateToLocal(this.teamName);
        }

        public TeamColorOuterClass.TeamColor toTeamColor() {
            return this.teamColor;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
