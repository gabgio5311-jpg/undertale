package com.example.undertale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Real Knife (comum): espada de netherite que fica mais forte conforme o LV do jogador.
 *
 * O dano em si é aplicado no {@link LvEvents#onRealKnifeHit} (evento, no servidor). Esta
 * classe só existe para mostrar na <b>tooltip</b> o bônus de dano que o LV atual concede,
 * já que a linha de "Dano de Ataque" do Minecraft é fixa e não conhece o LV do jogador.
 * O valor exibido vem do {@link ClientLvData} (LV do jogador local, sincronizado do servidor).
 *
 * <p>No 1.21.1 o dano/velocidade base vêm de {@code Item.Properties.attributes(...)} no
 * {@link ModItems}; o construtor do {@link SwordItem} só recebe {@code Tier + Properties}.
 */
public class RealKnifeItem extends SwordItem {

    public RealKnifeItem(Properties properties) {
        super(Tiers.NETHERITE, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Tooltip é renderizada no cliente, então lê o LV do cache sincronizado.
        int lv = ClientLvData.info().lv();
        int bonus = (int) ((lv - 1) * LvEvents.DANO_POR_LV);

        if (bonus > 0) {
            tooltipComponents.add(Component.literal("§eDamage per LV " + lv + ": §6+" + bonus));
        } else {
            tooltipComponents.add(Component.literal("§7Level up your LV for more damage").withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
