package com.example.undertale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Item simples que mostra no tooltip a chance de drop de mob.
 *
 * O drop em si é feito por evento ({@link ContadorMorteDropHandler} /
 * {@link SansBonesDropHandler}), então JEI/JER não conseguem ler essa chance
 * (não há loot table). Este tooltip é o jeito garantido de o jogador ver a %.
 *
 * Lê {@code baseChance}/{@code lootingBonus} passados no construtor (vindos das
 * constantes públicas do handler), então o texto acompanha qualquer mudança lá.
 */
public class DropInfoItem extends Item {

    private final float baseChance;
    private final float lootingBonus;
    private final String sourceText;
    /** Nível de Looting usado na linha "total" do tooltip (o servidor-alvo usa 10). */
    private static final int REFERENCE_LOOTING = 10;

    public DropInfoItem(Properties properties, float baseChance, float lootingBonus, String sourceText) {
        super(properties);
        this.baseChance = baseChance;
        this.lootingBonus = lootingBonus;
        this.sourceText = sourceText;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        float totalAtRef = baseChance + REFERENCE_LOOTING * lootingBonus;
        tooltip.add(Component.literal("Drops from " + sourceText).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Chance: " + pct(baseChance) + "% (+" + pct(lootingBonus)
                + "% per Looting level)").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("~" + pct(totalAtRef) + "% at Looting " + REFERENCE_LOOTING)
                .withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    /** Formata uma fração (0.005) como percentual enxuto ("0.5"), sem zeros/ponto sobrando. */
    private static String pct(float fraction) {
        String s = String.format(java.util.Locale.ROOT, "%.2f", fraction * 100.0F);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s;
    }
}
