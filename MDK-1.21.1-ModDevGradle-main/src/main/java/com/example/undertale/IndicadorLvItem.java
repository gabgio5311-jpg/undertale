package com.example.undertale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Indicador de LV: clique direito abre a tela de status; o tooltip mostra o LV/almas.
 * Os valores vêm do {@link ClientLvData} (cache do LV do jogador local, sincronizado
 * do servidor por {@link ModNetwork#syncLv}).
 */
public class IndicadorLvItem extends Item {

    public IndicadorLvItem(Properties properties) {
        super(properties);
    }

    /** Clique direito (com o item na mão) abre a tela de status do LV. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // A GUI é puramente client-side. Só abrimos no cliente; a classe da tela
        // (que referencia código de cliente) só é carregada aqui, nunca num servidor dedicado.
        if (level.isClientSide()) {
            IndicadorLvClient.open();
        }

        // SUCCESS no cliente / CONSUME no servidor: balança a mão sem disparar duas vezes.
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Tooltip é renderizado no cliente, então lê do cache sincronizado (ClientLvData).
        LvData.LvInfo info = ClientLvData.info();
        tooltipComponents.add(Component.literal("§eCurrent LV: §6" + info.lv()));
        if (info.isMax()) {
            tooltipComponents.add(Component.literal("§6MAX LV"));
        } else {
            tooltipComponents.add(Component.literal("§cSouls to next LV: §4"
                    + info.almasAtuais() + "/" + info.almasNecessarias()));
        }
        tooltipComponents.add(Component.literal("Right-click for details").withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
