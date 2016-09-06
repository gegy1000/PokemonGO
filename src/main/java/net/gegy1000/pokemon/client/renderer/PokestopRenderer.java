package net.gegy1000.pokemon.client.renderer;

import com.pokegoapi.api.map.fort.Pokestop;
import net.gegy1000.pokemon.client.renderer.model.PokestopModel;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class PokestopRenderer extends PokemonObjectRenderer<Pokestop> {
    private static final ResourceLocation POKESTOP_TEXTURE = new ResourceLocation("textures/entity/pig/pig.png");
    private static final PokestopModel POKESTOP_MODEL = new PokestopModel();

    @Override
    public void render(Pokestop pokestop, double x, double y, double z, float partialTicks) {
        MC.getTextureManager().bindTexture(POKESTOP_TEXTURE);
        GlStateManager.disableTexture2D();
        GlStateManager.color(pokestop.hasLure() ? 1.0F : 0.2F, 0.6F, 1.0F);
        GlStateManager.pushMatrix();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        GlStateManager.translate(x, y, z);
        float scale = 1.5F;
        GlStateManager.scale(scale, -scale, scale);
        GlStateManager.translate(0.0F, -1.5F, 0.0F);
        POKESTOP_MODEL.render(null, 0.0F, 0.0F, (int) (MC.thePlayer.ticksExisted + (pokestop.getLatitude() * 2000 * pokestop.getLongitude())) + partialTicks, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
    }
}
