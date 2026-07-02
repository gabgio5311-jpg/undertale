package com.example.undertale;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Faz o {@code contador_morte} dropar de mobs com uma chance baixa.
 *
 * Roda no bus GAME (auto-registrado). O {@link LivingDropsEvent} só dispara no
 * servidor, no momento da morte, e deixa a gente adicionar itens à lista de drops.
 */
@EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
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

        float chance = DROP_CHANCE + lootingLevel(entity, event.getSource()) * LOOTING_BONUS;
        if (entity.getRandom().nextFloat() >= chance) return;

        ItemStack stack = new ItemStack(ModItems.CONTADOR_MORTE.get());
        ItemEntity drop = new ItemEntity(entity.level(),
                entity.getX(), entity.getY(), entity.getZ(), stack);
        drop.setDefaultPickUpDelay();
        event.getDrops().add(drop);
    }

    /**
     * Nível de Looting da arma de quem matou. No 1.21 os encantamentos viraram dados/registro,
     * então pegamos o Holder do encantamento "looting" via registryAccess.
     */
    static int lootingLevel(LivingEntity victim, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity killer)) {
            return 0;
        }
        Holder<Enchantment> looting = victim.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.LOOTING);
        return EnchantmentHelper.getItemEnchantmentLevel(looting, killer.getMainHandItem());
    }
}
