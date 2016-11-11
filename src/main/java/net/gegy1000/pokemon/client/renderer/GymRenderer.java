package net.gegy1000.pokemon.client.renderer;

import POGOProtos.Data.PokemonDataOuterClass;
import com.pokegoapi.api.gym.Gym;
import net.gegy1000.pokemon.client.renderer.model.GymModel;
import net.gegy1000.pokemon.client.renderer.pokemon.DataRenderedPokemon;
import net.gegy1000.pokemon.client.renderer.pokemon.RenderedPokemon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GymRenderer extends PokemonObjectRenderer<Gym> {
    private static final ResourceLocation GYM_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper.png");
    private static final GymModel GYM_MODEL = new GymModel();

    @Override
    public void render(Gym gym, double x, double y, double z, float partialTicks) {
        MC.getTextureManager().bindTexture(GYM_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glNormal3f(1.0F, 1.0F, 1.0F);
        GlStateManager.disableTexture2D();
        RenderHelper.enableStandardItemLighting();
        MC.entityRenderer.enableLightmap();
        GlStateManager.pushMatrix();
        switch (gym.getOwnedByTeam()) {
            case BLUE:
                GlStateManager.color(0.0F, 0.0F, 1.0F, 1.0F);
                break;
            case NEUTRAL:
                GlStateManager.color(0.0F, 1.0F, 1.0F, 1.0F);
                break;
            case RED:
                GlStateManager.color(1.0F, 0.0F, 0.0F, 1.0F);
                break;
            case YELLOW:
                GlStateManager.color(1.0F, 1.0F, 0.0F, 1.0F);
                break;
            case UNRECOGNIZED:
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                break;
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        GlStateManager.translate(x, y, z);
        float scale = 1.25F;
        GlStateManager.scale(scale, -scale, scale);
        GlStateManager.translate(0.0F, -1.5F, 0.0F);
        GYM_MODEL.render(null, 0.0F, 0.0F, (MC.thePlayer.ticksExisted + ((int) (gym.getLatitude() * 2000 * gym.getLongitude()) & 0xFF)) + partialTicks, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
        if (MC.currentScreen == null) {
            //TODO Render Arguments
            try {
                GlStateManager.pushMatrix();
                GlStateManager.enableTexture2D();
                PokemonDataOuterClass.PokemonData defending = null;
                for (PokemonDataOuterClass.PokemonData pokemonData : gym.getDefendingPokemon()) {
                    if (defending == null || pokemonData.getCp() > defending.getCp()) {
                        defending = pokemonData;
                    }
                }
                if (defending != null) {
                    RenderedPokemon renderedPokemon = new DataRenderedPokemon(MC.theWorld, defending, 0xF000F0, false, true);
                    float pokemonScale = 0.5F;
                    GlStateManager.scale(pokemonScale, pokemonScale, pokemonScale);
                    RenderHandler.POKEMON_RENDERER.render(renderedPokemon, x / pokemonScale, (y + 5.5) / pokemonScale, z / pokemonScale, partialTicks);
                }
                GlStateManager.popMatrix();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
