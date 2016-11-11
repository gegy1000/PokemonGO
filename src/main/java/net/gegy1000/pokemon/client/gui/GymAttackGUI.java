package net.gegy1000.pokemon.client.gui;

import POGOProtos.Data.Battle.BattleActionOuterClass;
import POGOProtos.Data.Battle.BattleActionTypeOuterClass;
import POGOProtos.Data.Battle.BattlePokemonInfoOuterClass;
import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.pokemon.PokemonMoveMeta;
import com.pokegoapi.api.pokemon.PokemonMoveMetaRegistry;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.element.ModelViewElement;
import net.gegy1000.pokemon.client.renderer.RenderHandler;
import net.gegy1000.pokemon.client.renderer.pokemon.GymRenderedPokemon;
import net.gegy1000.pokemon.client.util.GymBattle;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GymAttackGUI extends PokemonGUI {
    private final Gym gym;
    private final Pokemon[] team;
    private GymBattle battle;
    private String name = "Unnamed";
    private long lastAttackTime;
    private long startClick;
    private long nextClick;
    private boolean clicked;

    private GymRenderedPokemon activeAttackerRender;
    private GymRenderedPokemon activeDefenderRender;

    private Thread updateThread;

    private ModelViewElement<GymAttackGUI> viewElement;

    public GymAttackGUI(Gym gym, Pokemon[] team) {
        this.gym = gym;
        this.team = team;
        PokemonHandler.addTask(() -> {
            try {
                PokemonHandler.API.getInventories().updateInventories(false);
                GymBattle battle = new GymBattle(PokemonHandler.API, this.gym, this.team);
                battle.start();
                this.battle = battle;
                this.updateThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        this.updateThread = new Thread(() -> {
            while (this.battle != null && this.battle.inProgress()) {
                long time = System.currentTimeMillis();
                if (time - this.lastAttackTime > 500) {
                    this.lastAttackTime = time;
                    try {
                        this.battle.sendQueuedActions();
                        this.updateRenderedPokemon();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (this.battle != null) {
                PokemonGO.LOGGER.info("Battle ended with state " + this.battle.getState());
                //TODO Add defeat / victory window
            }
        });
        try {
            this.name = gym.getName();
            this.updateRenderedPokemon();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.lastAttackTime = System.currentTimeMillis();
    }

    @Override
    public void initElements() {
        this.addElement(this.viewElement = new ModelViewElement<>(this, 0.0F, 46.0F, this.width, this.height - 46, (view) -> {
            GlStateManager.scale(1.0F, -1.0F, 1.0F);
            float partialTicks = LLibrary.PROXY.getPartialTicks();
            RenderHandler.GYM_RENDERER.render(this.gym, 0.0, -9.2, 0.0, partialTicks);
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.scale(0.25F, 0.25F, 0.25F);
            if (this.activeAttackerRender != null) {
                RenderHandler.POKEMON_RENDERER.render(this.activeAttackerRender, 2.0, -14.1, 5.0, partialTicks);
            }
            if (this.activeDefenderRender != null) {
                RenderHandler.POKEMON_RENDERER.render(this.activeDefenderRender, -9.0, -14.1, 5.0, partialTicks);
            }
            GlStateManager.disableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            return null;
        }, (mousePosition) -> {
            return false;
        }, LLibrary.CONFIG.getSecondaryColor(), false).withCameraOffset(0.0F, -2.65F).withZoom(1.75F).withRotation(45.0F, 5.0F));
    }

    @Override
    public void drawScreen(float mouseX, float mouseY, float partialTicks) {
        try {
            int textColor = LLibrary.CONFIG.getTextColor();

            this.drawRectangle(0, 0, this.width, 56.0F, LLibrary.CONFIG.getPrimaryColor());
            String titleString = I18n.translateToLocal("pokemon.attacking_gym.name") + " - " + this.name;
            this.fontRendererObj.drawString(titleString, this.width / 2 - this.fontRendererObj.getStringWidth(titleString) / 2, 6, textColor, false);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            Team gymTeam = new Team(this.gym.getOwnedByTeam());
            this.mc.getTextureManager().bindTexture(gymTeam.getTeamTexture());
            this.drawTexturedModalRect(2, 2, 0, 0, 32, 32, 32, 32, 1.0, 1.0);
            this.fontRendererObj.drawString(gymTeam.getTeamName(), 40, 15, textColor);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            Team team = new Team(PokemonHandler.API.getPlayerProfile().getPlayerData().getTeam());
            this.mc.getTextureManager().bindTexture(team.getTeamTexture());
            this.drawTexturedModalRect(this.width - 34, 2, 0, 0, 32, 32, 32, 32, 1.0, 1.0);
            this.fontRendererObj.drawString(team.getTeamName(), this.width - (this.fontRendererObj.getStringWidth(team.getTeamName()) + 40), 15, textColor);

            if (this.battle != null) {
                BattlePokemonInfoOuterClass.BattlePokemonInfo activeDefender = this.battle.getActiveDefender();
                if (activeDefender != null) {
                    PokemonDataOuterClass.PokemonData defenderData = activeDefender.getPokemonData();
                    String defenderName = defenderData.getNickname() == null || defenderData.getNickname().length() == 0 ? PokemonGUIHandler.getName(defenderData.getPokemonId()) : defenderData.getNickname();
                    String defenderText = I18n.translateToLocalFormatted("gui.gym_pokemon.name", defenderName, String.valueOf(defenderData.getCp()));
                    this.fontRendererObj.drawString(defenderText, 3, 36, textColor);
                    GlStateManager.disableTexture2D();
                    int healthX = this.fontRendererObj.getStringWidth(defenderText) + 8;
                    this.drawRectangle(healthX, 36, 100, 7, LLibrary.CONFIG.getSecondaryColor());
                    this.drawRectangle(healthX + 1, 37, activeDefender.getCurrentHealth() == 0 ? 0 : (activeDefender.getCurrentHealth() * 98) / defenderData.getStaminaMax(), 5, 0xFF4AD33C);
                }

                BattlePokemonInfoOuterClass.BattlePokemonInfo activeAttacker = this.battle.getActiveAttacker();
                if (activeAttacker != null) {
                    PokemonDataOuterClass.PokemonData attackerData = activeAttacker.getPokemonData();
                    String defenderName = attackerData.getNickname() == null || attackerData.getNickname().length() == 0 ? PokemonGUIHandler.getName(attackerData.getPokemonId()) : attackerData.getNickname();
                    String defenderText = I18n.translateToLocalFormatted("gui.gym_pokemon.name", defenderName, String.valueOf(attackerData.getCp()));
                    this.fontRendererObj.drawString(defenderText, this.width - this.fontRendererObj.getStringWidth(defenderText) - 3, 36, textColor);
                    GlStateManager.disableTexture2D();
                    int healthX = this.width - this.fontRendererObj.getStringWidth(defenderText) - 108;
                    this.drawRectangle(healthX, 36, 100, 7, LLibrary.CONFIG.getSecondaryColor());
                    this.drawRectangle(healthX + 1, 37, activeAttacker.getCurrentHealth() == 0 ? 0 : (activeAttacker.getCurrentHealth() * 98) / attackerData.getStaminaMax(), 5, 0xFF4AD33C);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isElementOnTop(this.viewElement)) {
            this.startClick = PokemonHandler.API.currentTimeMillis();
            this.clicked = true;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        long time = PokemonHandler.API.currentTimeMillis();
        long duration = time - this.startClick;
        if (this.clicked && this.battle != null && time > this.nextClick) { //TODO Special Attacks
            BattlePokemonInfoOuterClass.BattlePokemonInfo activeAttacker = this.battle.getActiveAttacker();
            PokemonMoveMeta moveMeta = PokemonMoveMetaRegistry.getMeta(activeAttacker.getPokemonData().getMove1());
            int totalAttackDuration = moveMeta == null ? 500 : moveMeta.getTime();
            long attackTime = this.battle.getServerTime() - totalAttackDuration;
            BattleActionOuterClass.BattleAction.Builder builder = BattleActionOuterClass.BattleAction.newBuilder();
            builder.setDurationMs(totalAttackDuration);
            builder.setType(BattleActionTypeOuterClass.BattleActionType.ACTION_ATTACK);
            builder.setActivePokemonId(activeAttacker.getPokemonData().getId());
            builder.setDamageWindowsStartTimestampMs(attackTime + totalAttackDuration - 200);
            builder.setDamageWindowsEndTimestampMs(attackTime + totalAttackDuration);
            builder.setActionStartMs(attackTime);
            builder.setTargetIndex(-1);
            this.battle.addActionToQueue(builder);
            this.nextClick = time + totalAttackDuration;
            PokemonGO.LOGGER.info("CLICK");
        }
        this.clicked = false;
    }

    private void updateRenderedPokemon() throws LoginFailedException, RemoteServerException {
        this.activeAttackerRender = null;
        this.activeDefenderRender = null;
        if (this.battle != null) {
            BattlePokemonInfoOuterClass.BattlePokemonInfo activeAttacker = this.battle.getActiveAttacker();
            if (activeAttacker != null && activeAttacker.getPokemonData().getPokemonId() != PokemonIdOuterClass.PokemonId.MISSINGNO) {
                this.activeAttackerRender = new GymRenderedPokemon(activeAttacker, -100.0F);
            }
            BattlePokemonInfoOuterClass.BattlePokemonInfo activeDefender = this.battle.getActiveDefender();
            if (activeDefender != null && activeDefender.getPokemonData().getPokemonId() != PokemonIdOuterClass.PokemonId.MISSINGNO) {
                this.activeDefenderRender = new GymRenderedPokemon(activeDefender, -280.0F);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.exit();
        PokemonHandler.addTask(() -> {
            try {
                PokemonHandler.API.getInventories().updateInventories();
                PokemonHandler.API.getPlayerProfile().updateProfile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    private void exit() {
        if (this.battle != null) {
            this.battle.end();
        }
    }
}