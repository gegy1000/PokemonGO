package net.gegy1000.pokemon.client.gui.view;

import POGOProtos.Enums.TeamColorOuterClass;
import com.pokegoapi.api.player.PlayerLevelUpRewards;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.LoginGUI;
import net.gegy1000.pokemon.client.gui.PokemonGUI;
import net.gegy1000.pokemon.pokemon.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
import net.ilexiconn.llibrary.client.gui.element.LabelElement;
import net.ilexiconn.llibrary.client.gui.element.ListElement;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class PokemonViewGUI extends PokemonGUI {
    private static final ResourceLocation NEUTRAL_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/neutral.png");
    private static final ResourceLocation INSTINCT_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/instinct.png");
    private static final ResourceLocation VALOR_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/valor.png");
    private static final ResourceLocation MYSTIC_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/mystic.png");

    private ButtonElement<PokemonViewGUI> character;
    private ButtonElement<PokemonViewGUI> nearby;
    private ButtonElement<PokemonViewGUI> inventory;
    private ButtonElement<PokemonViewGUI> statistics;

    private ViewMode viewMode = ViewMode.CHARACTER;

    @Override
    public void initElements() {
        if (PokemonHandler.GO == null) {
            WindowElement<PokemonViewGUI> window = new WindowElement<>(this, "Error!", 172, 50, false);
            new LabelElement<>(this, "You must be logged in to use this!", 2.0F, 18.0F).withParent(window);
            new ButtonElement<>(this, "Login", 1.0F, 30.0F, 84, 19, (button) -> {
                this.mc.displayGuiScreen(new LoginGUI());
                return true;
            }).withParent(window).withColorScheme(THEME_WINDOW);
            new ButtonElement<>(this, "Cancel", 87.0F, 30.0F, 84, 19, (button) -> {
                this.mc.displayGuiScreen(null);
                return true;
            }).withParent(window).withColorScheme(THEME_WINDOW);
            ElementHandler.INSTANCE.addElement(this, window);
        }
        ElementHandler.INSTANCE.addElement(this, this.character = (ButtonElement<PokemonViewGUI>) new ButtonElement<>(this, "Character", this.width - 240.0F, 0.0F, 60, 18, (button) -> {
            this.setViewMode(ViewMode.CHARACTER);
            return true;
        }).withColorScheme(THEME_TAB_ACTIVE));
        ElementHandler.INSTANCE.addElement(this, this.nearby = new ButtonElement<>(this, "Nearby", this.width - 180.0F, 0.0F, 60, 18, (button) -> {
            this.setViewMode(ViewMode.NEARBY);
            return true;
        }));
        ElementHandler.INSTANCE.addElement(this, this.inventory = new ButtonElement<>(this, "Inventory", this.width - 120.0F, 0.0F, 60, 18, (button) -> {
            this.setViewMode(ViewMode.INVENTORY);
            return true;
        }));
        ElementHandler.INSTANCE.addElement(this, this.statistics = new ButtonElement<>(this, "Statistics", this.width - 60.0F, 0.0F, 60, 18, (button) -> {
            this.setViewMode(ViewMode.STATISTICS);
            return true;
        }));
        ElementHandler.INSTANCE.addElement(this, new ButtonElement<>(this, "+", this.width - 34.0F, this.height - 34.0F, 34, 34, (button) -> {
            WindowElement<PokemonViewGUI> window = new WindowElement<>(this, "Level Up Rewards", 120, 164, true);
            List<String> entries = new ArrayList<>();
            int level = 1;
            try {
                level = PokemonHandler.GO.getPlayerProfile().getStats().getLevel();
            } catch (Exception e) {
            }
            for (int i = 1; i <= 40; i++) {
                if (i <= level) {
                    entries.add("Level " + i);
                }
            }
            new ListElement<>(this, 0.0F, 14.0F, 120, 150, entries, (list) -> {
                new Thread(() -> {
                    try {
                        PlayerLevelUpRewards rewards = PokemonHandler.GO.getPlayerProfile().acceptLevelUpRewards(list.getSelectedIndex() + 1);
                        String statusWindowTitle = null;
                        String statusWindowMessage = null;
                        switch (rewards.getStatus()) {
                            case ALREADY_ACCEPTED:
                                statusWindowTitle = "Failure!";
                                statusWindowMessage = "You have already accepted this reward.";
                                break;
                            case NOT_UNLOCKED_YET:
                                statusWindowTitle = "Failure!";
                                statusWindowMessage = "You have not unlocked this yet!";
                                break;
                            case NEW:
                                statusWindowTitle = "Success!";
                                statusWindowMessage = "Your rewards have been added to your inventory.";
                                break;
                        }
                        if (statusWindowMessage != null) {
                            int windowWidth = this.fontRendererObj.getStringWidth(statusWindowMessage) + 4;
                            WindowElement<PokemonViewGUI> resultWindow = new WindowElement<>(this, statusWindowTitle, windowWidth, 45, false);
                            new LabelElement<>(this, statusWindowMessage, 2, 18).withParent(resultWindow);
                            new ButtonElement<>(this, "Okay", 1, 29, windowWidth - 2, 15, (btn) -> {
                                ElementHandler.INSTANCE.removeElement(this, resultWindow);
                                return true;
                            }).withParent(resultWindow).withColorScheme(THEME_WINDOW);
                            ElementHandler.INSTANCE.addElement(this, resultWindow);
                        }
                        PokemonHandler.GO.getInventories().updateInventories();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                ElementHandler.INSTANCE.removeElement(this, window);
                return true;
            }).withParent(window);
            ElementHandler.INSTANCE.addElement(this, window);
            return true;
        }));
        this.setViewMode(ViewMode.CHARACTER);
    }

    @Override
    public void drawScreen(float mouseX, float mouseY, float partialTicks) {
        this.drawRectangle(0, this.height - 34.0F, this.width, 34.0F, LLibrary.CONFIG.getPrimaryColor());
        this.drawRectangle(0, 0, this.width, 18.0F, LLibrary.CONFIG.getPrimaryColor());
        this.fontRendererObj.drawString("Pokémon View", 5, 5, LLibrary.CONFIG.getTextColor());
        if (PokemonHandler.GO != null) {
            try {
                this.fontRendererObj.drawString(PokemonHandler.username, this.width - this.fontRendererObj.getStringWidth(PokemonHandler.username) - 39, this.height - 20, LLibrary.CONFIG.getTextColor());
                TeamColorOuterClass.TeamColor team = PokemonHandler.GO.getPlayerProfile().getPlayerData().getTeam();
                ResourceLocation teamTexture = NEUTRAL_TEXTURE;
                String teamName = "Neutral";
                switch (team) {
                    case YELLOW:
                        teamTexture = INSTINCT_TEXTURE;
                        teamName = "Instinct";
                        break;
                    case RED:
                        teamTexture = VALOR_TEXTURE;
                        teamName = "Valor";
                        break;
                    case BLUE:
                        teamTexture = MYSTIC_TEXTURE;
                        teamName = "Mystic";
                        break;
                }
                this.mc.getTextureManager().bindTexture(teamTexture);
                this.drawTexturedModalRect(0, this.height - 32, 0, 0, 32, 32, 32, 32, 1.0, 1.0);
                this.fontRendererObj.drawString(teamName, 35, this.height - 20, LLibrary.CONFIG.getTextColor());

                this.viewMode.getViewHandler().render(mouseX, mouseY, partialTicks);
            } catch (Exception e) {
            }
        }
    }

    public void setViewMode(ViewMode viewMode) {
        ViewMode prevViewMode = this.viewMode;

        if (prevViewMode != null) {
            prevViewMode.getButton(this).withColorScheme(ButtonElement.DEFAULT);
        }

        this.viewMode = viewMode;
        this.viewMode.getButton(this).withColorScheme(THEME_TAB_ACTIVE);
        this.viewMode.getViewHandler().setGUI(this);

        if (prevViewMode != null) {
            prevViewMode.getViewHandler().cleanupView();
        }

        this.viewMode.getViewHandler().initView();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.viewMode.getViewHandler().cleanupView();
    }

    public enum ViewMode {
        CHARACTER((gui) -> gui.character, new CharacterViewHandler()),
        NEARBY((gui) -> gui.nearby, new NearbyViewHandler()),
        INVENTORY((gui) -> gui.inventory, new InventoryViewHandler()),
        STATISTICS((gui) -> gui.statistics, new StatisticsViewHandler());

        private Function<PokemonViewGUI, ButtonElement<PokemonViewGUI>> button;
        private ViewHandler viewHandler;

        ViewMode(Function<PokemonViewGUI, ButtonElement<PokemonViewGUI>> button, ViewHandler viewHandler) {
            this.button = button;
            this.viewHandler = viewHandler;
        }

        public ButtonElement<PokemonViewGUI> getButton(PokemonViewGUI gui) {
            return this.button.apply(gui);
        }

        public ViewHandler getViewHandler() {
            return this.viewHandler;
        }
    }
}
