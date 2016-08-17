package net.gegy1000.pokemon.client.gui.view;

import com.pokegoapi.api.inventory.Stats;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class StatisticsViewHandler extends ViewHandler {
    private List<String> statistics = new LinkedList<>();

    public StatisticsViewHandler(PokemonViewGUI gui) {
        super(gui);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        try {
            int x = 3;
            int y = 21;
            for (String stat : this.statistics) {
                this.fontRenderer.drawString("- " + stat, x, y, LLibrary.CONFIG.getTextColor());
                y += 14;
                if (y > this.getGUI().height - 40) {
                    y = 21;
                    x += 170;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY) {

    }

    @Override
    public void initView() {
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
            DecimalFormat shortDecimalFormat = new DecimalFormat("#.##", symbols);
            Stats statistics = PokemonHandler.GO.getPlayerProfile().getStats();
            this.statistics.add(I18n.translateToLocal("stat.level.name") + " " + TextFormatting.DARK_GREEN + statistics.getLevel());
            this.statistics.add(I18n.translateToLocal("stat.experience.name") + " " + TextFormatting.DARK_GREEN + statistics.getExperience() + "XP (" + (int) ((statistics.getExperience() - statistics.getPrevLevelXp()) / (double) statistics.getNextLevelXp() * 100.0) + "%)");
            this.statistics.add(I18n.translateToLocal("stat.pokeballs_thrown.name") + " " + TextFormatting.BLUE + statistics.getPokeballsThrown());
            this.statistics.add(I18n.translateToLocal("stat.pokemon_deployed.name") + " " + TextFormatting.BLUE + statistics.getPokemonDeployed());
            this.statistics.add(I18n.translateToLocal("stat.pokemon_captured.name") + " " + TextFormatting.BLUE + statistics.getPokemonsCaptured());
            this.statistics.add(I18n.translateToLocal("stat.pokemon_encountered.name") + " " + TextFormatting.BLUE + statistics.getPokemonsEncountered());
            this.statistics.add(I18n.translateToLocal("stat.pokestop_visits.name") + " " + TextFormatting.BLUE + statistics.getPokeStopVisits());
            this.statistics.add(I18n.translateToLocal("stat.small_rattata_caught.name") + " " + TextFormatting.BLUE + statistics.getSmallRattataCaught());
            this.statistics.add(I18n.translateToLocal("stat.unique_pokemon.name") + " " + TextFormatting.GOLD + statistics.getUniquePokedexEntries());
            this.statistics.add(I18n.translateToLocal("stat.evolutions.name") + " " + TextFormatting.GOLD + statistics.getEvolutions());
            this.statistics.add(I18n.translateToLocal("stat.eggs_hatched.name") + " " + TextFormatting.GOLD + statistics.getEggsHatched());
            this.statistics.add(I18n.translateToLocal("stat.distance_walked.name") + " " + TextFormatting.GOLD + shortDecimalFormat.format(statistics.getKmWalked()) + "km");
            this.statistics.add(I18n.translateToLocal("stat.big_magikarp_caught.name") + " " + TextFormatting.GOLD + statistics.getBigMagikarpCaught());
            this.statistics.add(I18n.translateToLocal("stat.total_battle_attacks.name") + " " + TextFormatting.RED + statistics.getBattleAttackTotal());
            this.statistics.add(I18n.translateToLocal("stat.won_battle_attacks.name") + " " + TextFormatting.RED + statistics.getBattleAttackWon());
            this.statistics.add(I18n.translateToLocal("stat.won_defenses.name") + " " + TextFormatting.RED + statistics.getBattleDefendedWon());
            this.statistics.add(I18n.translateToLocal("stat.total_training_battles.name") + " " + TextFormatting.RED + statistics.getBattleTrainingTotal());
            this.statistics.add(I18n.translateToLocal("stat.won_training_battles.name") + " " + TextFormatting.RED + statistics.getBattleTrainingWon());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanupView() {
        this.statistics.clear();
    }
}
