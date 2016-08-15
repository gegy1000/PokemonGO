package net.gegy1000.pokemon.client.event;

import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.util.PokeNames;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.CapturePokemonGUI;
import net.gegy1000.pokemon.client.gui.LoginGUI;
import net.gegy1000.pokemon.client.gui.PokestopGUI;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.gegy1000.pokemon.client.key.PokemonKeyBinds;
import net.gegy1000.pokemon.pokemon.PokemonHandler;
import net.gegy1000.pokemon.pokemon.PokemonSpriteHandler;
import net.gegy1000.pokemon.server.world.gen.WorldTypePokemonEarth;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private int pokemonDisplayList;
    private boolean pokemonListCompiled;

    private static final ResourceLocation GYM_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper.png");
    private static final ModelCreeper GYM_MODEL = new ModelCreeper();

    private static final ResourceLocation POKESTOP_TEXTURE = new ResourceLocation("textures/entity/pig/pig.png");
    private static final ModelPig POKESTOP_MODEL = new ModelPig();

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (MC.theWorld.getWorldType() instanceof WorldTypePokemonEarth) {
            if (PokemonKeyBinds.KEY_POKEMON_VIEW.isPressed()) {
                MC.displayGuiScreen(new PokemonViewGUI());
            } else if (PokemonKeyBinds.KEY_LOGIN.isPressed()) {
                MC.displayGuiScreen(new LoginGUI());
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        PokemonHandler.update(MC.thePlayer);
    }

    @SubscribeEvent
    public void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        this.interactMapObjects();
    }

    @SubscribeEvent
    public void onRightClickEmpty(PlayerInteractEvent.RightClickBlock event) {
        this.interactMapObjects();
    }

    private void interactMapObjects() {
        if (PokemonHandler.GO != null && MC.currentScreen == null) {
            try {
                Map<Pokestop, AxisAlignedBB> pokestopBounds = new HashMap<>();
                EntityPlayerSP player = MC.thePlayer;
                Map<CatchablePokemon, AxisAlignedBB> pokemonBounds = new HashMap<>();
                for (CatchablePokemon pokemon : PokemonHandler.GO.getMap().getCatchablePokemon()) {
                    double x = PokemonGO.GENERATOR.fromLong(pokemon.getLongitude());
                    double z = PokemonGO.GENERATOR.fromLat(pokemon.getLatitude());
                    int y = player.worldObj.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
                    pokemonBounds.put(pokemon, new AxisAlignedBB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.0, z + 0.3));
                }
                CatchablePokemon pokemon = this.getInteract(player, pokemonBounds);
                if (pokemon != null) {
                    MC.displayGuiScreen(new CapturePokemonGUI(pokemon));
                } else {
                    for (Pokestop pokestop : PokemonHandler.GO.getMap().getMapObjects().getPokestops()) {
                        double x = PokemonGO.GENERATOR.fromLong(pokestop.getLongitude());
                        double z = PokemonGO.GENERATOR.fromLat(pokestop.getLatitude());
                        int y = player.worldObj.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
                        pokestopBounds.put(pokestop, new AxisAlignedBB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.0, z + 0.3));
                    }
                    Pokestop pokestop = this.getInteract(player, pokestopBounds);
                    if (pokestop != null) {
                        MC.displayGuiScreen(new PokestopGUI(pokestop));
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private <T> T getInteract(EntityPlayer player, Map<T, AxisAlignedBB> bounds) {
        Vec3d eyePosition = player.getPositionEyes(1.0F);
        double reach = 6.0;
        Vec3d look = player.getLook(1.0F);
        Vec3d farLook = eyePosition.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        double closestDistance = reach;
        T interact = null;
        for (Map.Entry<T, AxisAlignedBB> entry : bounds.entrySet()) {
            AxisAlignedBB bound = entry.getValue();
            RayTraceResult intercept = bound.calculateIntercept(eyePosition, farLook);
            if (bound.isVecInside(eyePosition)) {
                if (closestDistance >= 0.0D) {
                    interact = entry.getKey();
                    closestDistance = 0.0D;
                }
            } else if (intercept != null) {
                double distance = eyePosition.distanceTo(intercept.hitVec);
                if (distance < closestDistance || closestDistance == 0.0D) {
                    interact = entry.getKey();
                    closestDistance = distance;
                }
            }
        }
        return interact;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (PokemonHandler.GO != null && MC.theWorld.getWorldType() instanceof WorldTypePokemonEarth) {
            this.renderPokemonFeatures();
        }
    }

    private void renderPokemonFeatures() {
        if (!this.pokemonListCompiled) {
            this.pokemonDisplayList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(this.pokemonDisplayList, GL11.GL_COMPILE);
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(0.0, 1.0, -0.5).tex(0.0, 1.0).endVertex();
            buffer.pos(0.0, 1.0, 0.5).tex(1.0, 1.0).endVertex();
            buffer.pos(0.0, 0.0, 0.5).tex(1.0, 0.0).endVertex();
            buffer.pos(0.0, 0.0, -0.5).tex(0.0, 0.0).endVertex();
            buffer.pos(0.025, 1.0, -0.5).tex(0.0, 1.0).endVertex();
            buffer.pos(0.025, 1.0, 0.5).tex(1.0, 1.0).endVertex();
            buffer.pos(0.025, 0.0, 0.5).tex(1.0, 0.0).endVertex();
            buffer.pos(0.025, 0.0, -0.5).tex(0.0, 0.0).endVertex();
            tessellator.draw();

            float scale = 1.0F / 96.0F;

            for (int lineX = 0; lineX < 96; lineX++) {
                float textureX = lineX * scale;
                float textureX2 = (lineX + 1) * scale;
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(0.0, 0.0, textureX - 0.5).tex(textureX, 0.0).endVertex();
                buffer.pos(0.0, 1.0, textureX - 0.5).tex(textureX, 1.0).endVertex();
                buffer.pos(0.025, 1.0, textureX - 0.5).tex(textureX2, 1.0).endVertex();
                buffer.pos(0.025, 0.0, textureX - 0.5).tex(textureX2, 0.0).endVertex();
                tessellator.draw();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(0.0, 0.0, textureX2 - 0.5).tex(textureX, 0.0).endVertex();
                buffer.pos(0.0, 1.0, textureX2 - 0.5).tex(textureX, 1.0).endVertex();
                buffer.pos(0.025, 1.0, textureX2 - 0.5).tex(textureX2, 1.0).endVertex();
                buffer.pos(0.025, 0.0, textureX2 - 0.5).tex(textureX2, 0.0).endVertex();
                tessellator.draw();
            }

            for (int lineY = 0; lineY < 96; lineY++) {
                float textureY = lineY * scale;
                float textureY2 = (lineY + 1) * scale;
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(0.0, textureY, -0.5).tex(0.0, textureY).endVertex();
                buffer.pos(0.0, textureY, 0.5).tex(1.0, textureY).endVertex();
                buffer.pos(0.025, textureY, 0.5).tex(1.0, textureY2).endVertex();
                buffer.pos(0.025, textureY, -0.5).tex(0.0, textureY2).endVertex();
                tessellator.draw();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(0.0, textureY2, -0.5).tex(0.0, textureY).endVertex();
                buffer.pos(0.0, textureY2, 0.5).tex(1.0, textureY).endVertex();
                buffer.pos(0.025, textureY2, 0.5).tex(1.0, textureY2).endVertex();
                buffer.pos(0.025, textureY2, -0.5).tex(0.0, textureY2).endVertex();
                tessellator.draw();
            }
            GlStateManager.glEndList();
            this.pokemonListCompiled = true;
        }
        try {
            EntityPlayerSP player = MC.thePlayer;
            float partialTicks = LLibrary.PROXY.getPartialTicks();
            double viewX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
            double viewY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
            double viewZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
            GlStateManager.disableCull();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();
            MC.getTextureManager().bindTexture(GYM_TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            for (Gym gym : PokemonHandler.GO.getMap().getGyms()) {
                GlStateManager.pushMatrix();
                double x = PokemonGO.GENERATOR.fromLong(gym.getLongitude());
                double z = PokemonGO.GENERATOR.fromLat(gym.getLatitude());
                int y = player.worldObj.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
                GlStateManager.translate(x - viewX, y - viewY, z - viewZ);
                GlStateManager.scale(1.0F, -1.0F, 1.0F);
                GlStateManager.translate(0.0F, -1.5F, 0.0F);
                GYM_MODEL.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                GlStateManager.popMatrix();
            }
            MC.getTextureManager().bindTexture(POKESTOP_TEXTURE);
            for (Pokestop pokestop : PokemonHandler.GO.getMap().getMapObjects().getPokestops()) {
                GlStateManager.pushMatrix();
                double x = PokemonGO.GENERATOR.fromLong(pokestop.getLongitude());
                double z = PokemonGO.GENERATOR.fromLat(pokestop.getLatitude());
                int y = player.worldObj.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
                GlStateManager.translate(x - viewX, y - viewY, z - viewZ);
                GlStateManager.scale(1.0F, -1.0F, 1.0F);
                GlStateManager.translate(0.0F, -1.5F, 0.0F);
                POKESTOP_MODEL.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                GlStateManager.popMatrix();
            }
            RenderHelper.enableStandardItemLighting();
            MC.entityRenderer.enableLightmap();
            GL11.glNormal3f(1.0F, 1.0F, 1.0F);
            for (CatchablePokemon pokemon : PokemonHandler.GO.getMap().getCatchablePokemon()) {
                AdvancedDynamicTexture texture = PokemonSpriteHandler.get(pokemon.getPokemonId());
                GlStateManager.pushMatrix();
                double x = PokemonGO.GENERATOR.fromLong(pokemon.getLongitude());
                double z = PokemonGO.GENERATOR.fromLat(pokemon.getLatitude());
                int y = player.worldObj.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
                int light = player.worldObj.getCombinedLight(new BlockPos(x, y, z), 0);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536.0F);
                double deltaX = x - viewX;
                double deltaZ = z - viewZ;
                GlStateManager.translate(deltaX, y - viewY, deltaZ);
                GlStateManager.scale(2.0F, -2.0F, 2.0F);
                GlStateManager.translate(0.0F, -0.8F, 0.0F);
                float timer = (float) (player.ticksExisted + x + z + partialTicks);
                GlStateManager.rotate(-MC.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, Math.sin(timer * 0.125F) * 0.025F, 0.0F);
                if (texture != null) {
                    texture.bind();
                    GlStateManager.callList(this.pokemonDisplayList);
                }
                GlStateManager.popMatrix();
                double distance = deltaX * deltaX + deltaZ * deltaZ;
                if (distance <= 1024) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(deltaX, y + 1.25F - viewY, deltaZ);
                    String name = PokeNames.getDisplayName(pokemon.getPokemonId().getNumber(), Locale.ENGLISH);
                    GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(-MC.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate((MC.getRenderManager().options.thirdPersonView == 2 ? -1 : 1) * MC.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                    GlStateManager.scale(-0.025F, -0.025F, 0.025F);
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
                    fontRenderer.drawString(name, -fontRenderer.getStringWidth(name) / 2, 0, 553648127);
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
            MC.entityRenderer.disableLightmap();
        } catch (Exception e) {
        }
    }
}
