package net.gegy1000.pokemon.client.event;

import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.CapturePokemonGUI;
import net.gegy1000.pokemon.client.gui.GymGUI;
import net.gegy1000.pokemon.client.gui.LoginGUI;
import net.gegy1000.pokemon.client.gui.PokestopGUI;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.gegy1000.pokemon.client.key.PokemonKeyBinds;
import net.gegy1000.pokemon.client.renderer.RenderHandler;
import net.gegy1000.pokemon.client.renderer.pokemon.CatchableRenderedPokemon;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonMapHandler;
import net.gegy1000.pokemon.server.world.gen.WorldTypePokemonEarth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();

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
        if (event.getEntityPlayer() == MC.thePlayer) {
            this.interactMapObjects();
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer() == MC.thePlayer) {
            this.interactMapObjects();
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntityPlayer() == MC.thePlayer) {
            this.interactMapObjects();
        }
    }

    private void interactMapObjects() {
        if (PokemonHandler.API != null && MC.currentScreen == null) {
            try {
                EntityPlayerSP player = MC.thePlayer;
                Map<CatchablePokemon, AxisAlignedBB> pokemonBounds = new HashMap<>();
                for (CatchablePokemon pokemon : PokemonMapHandler.getCatchablePokemon()) {
                    double x = PokemonGO.GENERATOR.fromLong(pokemon.getLongitude());
                    double z = PokemonGO.GENERATOR.fromLat(pokemon.getLatitude());
                    int y = player.worldObj.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
                    pokemonBounds.put(pokemon, new AxisAlignedBB(x - 2.0, y, z - 2.0, x + 2.0, y + 4.0, z + 2.0));
                }
                CatchablePokemon pokemon = this.getInteract(player, pokemonBounds);
                if (pokemon != null) {
                    MC.displayGuiScreen(new CapturePokemonGUI(pokemon));
                    return;
                }

                Map<Pokestop, AxisAlignedBB> pokestopBounds = new HashMap<>();
                for (Pokestop pokestop : PokemonMapHandler.getPokestops()) {
                    double x = PokemonGO.GENERATOR.fromLong(pokestop.getLongitude());
                    double z = PokemonGO.GENERATOR.fromLat(pokestop.getLatitude());
                    int y = player.worldObj.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
                    pokestopBounds.put(pokestop, new AxisAlignedBB(x - 2.0, y, z - 2.0, x + 2.0, y + 6.0, z + 3.0));
                }
                Pokestop pokestop = this.getInteract(player, pokestopBounds);
                if (pokestop != null) {
                    MC.displayGuiScreen(new PokestopGUI(pokestop));
                    return;
                }

                Map<Gym, AxisAlignedBB> gymBounds = new HashMap<>();
                for (Gym gym : PokemonMapHandler.getGyms()) {
                    double x = PokemonGO.GENERATOR.fromLong(gym.getLongitude());
                    double z = PokemonGO.GENERATOR.fromLat(gym.getLatitude());
                    int y = player.worldObj.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
                    gymBounds.put(gym, new AxisAlignedBB(x - 3.0, y, z - 3.0, x + 3.0, y + 12.0, z + 3.0));
                }
                Gym gym = this.getInteract(player, gymBounds);
                if (gym != null) {
                    MC.displayGuiScreen(new GymGUI(gym));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private <T> T getInteract(EntityPlayer player, Map<T, AxisAlignedBB> bounds) {
        Vec3d eyePosition = player.getPositionEyes(1.0F);
        double reach = 8.0;
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
        if (PokemonHandler.API != null && MC.theWorld.getWorldType() instanceof WorldTypePokemonEarth) {
            this.renderPokemonFeatures(event.getPartialTicks());
        }
    }

    private void renderPokemonFeatures(float partialTicks) {
        try {
            EntityPlayerSP player = MC.thePlayer;
            double viewX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
            double viewY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
            double viewZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableCull();
            GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
            GlStateManager.enableBlend();
            GlStateManager.enableFog();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            synchronized (PokemonMapHandler.MAP_LOCK) {
                List<Gym> gyms = PokemonMapHandler.getGyms();
                for (Gym gym : gyms) {
                    if (gym != null) {
                        double x = PokemonGO.GENERATOR.fromLong(gym.getLongitude());
                        double z = PokemonGO.GENERATOR.fromLat(gym.getLatitude());
                        int y = player.worldObj.getHeight(pos.setPos(x, 0, z)).getY();
                        RenderHandler.GYM_RENDERER.render(gym, x - viewX, y - viewY, z - viewZ, partialTicks);
                    }
                }
                GlStateManager.enableTexture2D();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                List<Pokestop> pokestops = PokemonMapHandler.getPokestops();
                for (Pokestop pokestop : pokestops) {
                    if (pokestop != null) {
                        double x = PokemonGO.GENERATOR.fromLong(pokestop.getLongitude());
                        double z = PokemonGO.GENERATOR.fromLat(pokestop.getLatitude());
                        int y = player.worldObj.getHeight(pos.setPos(x, 0, z)).getY();
                        RenderHandler.POKESTOP_RENDERER.render(pokestop, x - viewX, y - viewY, z - viewZ, partialTicks);
                    }
                }
                GlStateManager.disableCull();
                List<CatchableRenderedPokemon> renderedPokemon = PokemonMapHandler.getCatchableRenderedPokemon();
                for (CatchableRenderedPokemon pokemon : renderedPokemon) {
                    CatchablePokemon catchablePokemon = pokemon.getPokemon();
                    double x = PokemonGO.GENERATOR.fromLong(catchablePokemon.getLongitude());
                    double z = PokemonGO.GENERATOR.fromLat(catchablePokemon.getLatitude());
                    int y = player.worldObj.getHeight(pos.setPos(x, 0, z)).getY();
                    RenderHandler.POKEMON_RENDERER.render(new CatchableRenderedPokemon(MC.theWorld, catchablePokemon, true, true), x - viewX, y - viewY, z - viewZ, partialTicks);
                }
            }
            GlStateManager.enableCull();
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.disableFog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
