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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IndicadorLvItem extends Item {

    public IndicadorLvItem(Properties properties) {
        super(properties);
    }

    /** Clique direito (com o item na mão) abre a tela de status do LV. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // A GUI é puramente client-side. Só abrimos no cliente e usamos DistExecutor
        // para que a classe da tela (que referencia código de cliente) NUNCA seja
        // carregada num servidor dedicado.
        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> IndicadorLvClient::open);
        }

        // SUCCESS no cliente / CONSUME no servidor: balança a mão sem disparar duas vezes.
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Tooltip é renderizado no cliente, então lê do cache sincronizado (ClientLvData).
        LvData.LvInfo info = ClientLvData.info();
        tooltipComponents.add(Component.literal("§eLV atual: §6" + info.lv()));
        if (info.isMax()) {
            tooltipComponents.add(Component.literal("§6LV MÁXIMO"));
        } else {
            tooltipComponents.add(Component.literal("§cAlmas para o próximo LV: §4"
                    + info.almasAtuais() + "/" + info.almasNecessarias()));
        }
        tooltipComponents.add(Component.literal("Clique direito para ver detalhes").withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
