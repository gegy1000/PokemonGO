package net.gegy1000.pokemon.client.renderer.pokemon;

import POGOProtos.Enums.PokemonIdOuterClass;
import net.gegy1000.pokemon.client.renderer.PokemonObjectRenderer;
import net.gegy1000.pokemon.client.renderer.RenderHandler;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class PokemonRenderer extends PokemonObjectRenderer<RenderedPokemon> {
    @Override
    public void render(RenderedPokemon pokemon, double x, double y, double z, float partialTicks) {
        MC.entityRenderer.enableLightmap();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        int light = pokemon.getLight();
        GL11.glNormal3f(1.0F, 1.0F, 1.0F);
        float scale = 4.5F;
        GlStateManager.scale(scale, -scale, scale);
        GlStateManager.translate(0.0F, -0.8F, 0.0F);
        GlStateManager.rotate((pokemon.shouldFacePlayer() ? -MC.getRenderManager().playerViewY : 0.0F) + pokemon.getRenderYaw(), 0.0F, 1.0F, 0.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536.0F);
        PokemonIdOuterClass.PokemonId pokemonID = pokemon.getPokemonID();
        ModelBase model = RenderHandler.getPokemonModel(pokemonID);
        if (RenderHandler.bindPokemonTexture(pokemonID)) {
            model.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        } else {
            GlStateManager.popMatrix();
            MC.entityRenderer.disableLightmap();
            return;
        }
        GlStateManager.popMatrix();
        MC.entityRenderer.disableLightmap();
        if (pokemon.shouldRenderName()) {
            double distance = x * x + z * z;
            if (distance <= 1024) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y + (0.7F * scale), z);
                String name = PokemonGUIHandler.getName(pokemon.getPokemonID());
                GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-MC.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate((MC.getRenderManager().options.thirdPersonView == 2 ? -1 : 1) * MC.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                float textScale = scale * 0.015F;
                GlStateManager.scale(-textScale, -textScale, textScale);
                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                FontRenderer fontRenderer = MC.fontRendererObj;
                int center = fontRenderer.getStringWidth(name) / 2;
                GlStateManager.disableTexture2D();
                Tessellator tessellator = Tessellator.getInstance();
                VertexBuffer buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(-center - 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                buffer.pos(-center - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                buffer.pos(center + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                buffer.pos(center + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
                fontRenderer.drawString(name, -fontRenderer.getStringWidth(name) / 2, 0, 0x20FFFFFF);
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                fontRenderer.drawString(name, -fontRenderer.getStringWidth(name) / 2, 0, -1);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
        }
    }
}
