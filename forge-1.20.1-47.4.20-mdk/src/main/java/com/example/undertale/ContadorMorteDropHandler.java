package com.example.undertale;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Faz o {@code contador_morte} dropar de mobs com uma chance baixa.
 *
 * Roda no bus FORGE (auto-registrado). O {@link LivingDropsEvent} só dispara no
 * servidor, no momento da morte, e deixa a gente adicionar itens à lista de drops.
 */
@Mod.EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ContadorMorteDropHandler {

    /** Chance base de drop (sem looting). 0.005 = 0.5%. Mantido bem baixo (servidor usa Looting 10). */
    public static final float DROP_CHANCE = 0.005F;

    /** Bônus por nível de Saque/Looting. 0.001 = +0.1% por nível → no Looting 10 fica ~1.5% total. */
    public static final float LOOTING_BONUS = 0.001F;

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();

        // Só de mobs de verdade (exclui jogadores, armor stands, etc.).
        if (!(entity instanceof Mob)) return;

        // Se quiser que só conte quando um JOGADOR matar, descomente:
        // if (!(event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player)) return;

        float chance = DROP_CHANCE + event.getLootingLevel() * LOOTING_BONUS;
        if (entity.getRandom().nextFloat() >= chance) return;

        ItemStack stack = new ItemStack(ModItems.CONTADOR_MORTE.get());
        ItemEntity drop = new ItemEntity(entity.level(),
                entity.getX(), entity.getY(), entity.getZ(), stack);
        drop.setDefaultPickUpDelay();
        event.getDrops().add(drop);
    }
}
