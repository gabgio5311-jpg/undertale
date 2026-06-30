package com.example.undertale;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IndicadorLvItem extends Item {
    public IndicadorLvItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Por enquanto, vamos deixar um valor fixo de teste.
        // No futuro, quando o sistema de LV estiver pronto, pegaremos o valor real do jogador!
        int killsRestantes = 5;
        int lvAtual = 1;

        tooltipComponents.add(Component.literal("§eLV atual: §6" + lvAtual));
        tooltipComponents.add(Component.literal("§cAlmas necessárias para o próximo LV: §4" + killsRestantes));

        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
