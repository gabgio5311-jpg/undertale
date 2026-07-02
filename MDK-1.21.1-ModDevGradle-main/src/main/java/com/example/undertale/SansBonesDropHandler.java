package com.example.undertale;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Faz o {@code sans_bones} ("Blue Bones") dropar com chance baixa — igual ao
 * {@link ContadorMorteDropHandler}, mas SÓ de ESQUELETOS.
 *
 * Usa {@link AbstractSkeleton}, então cobre Skeleton, Stray e Wither Skeleton.
 */
@EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SansBonesDropHandler {

    /** Chance base de drop (sem looting). 0.005 = 0.5%. Mantido bem baixo (servidor usa Looting 10). */
    public static final float DROP_CHANCE = 0.005F;

    /** Bônus por nível de Saque/Looting. 0.001 = +0.1% por nível → no Looting 10 fica ~1.5% total. */
    public static final float LOOTING_BONUS = 0.001F;

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();

        // Só de esqueletos.
        if (!(entity instanceof AbstractSkeleton)) return;

        float chance = DROP_CHANCE + ContadorMorteDropHandler.lootingLevel(entity, event.getSource()) * LOOTING_BONUS;
        if (entity.getRandom().nextFloat() >= chance) return;

        ItemStack stack = new ItemStack(ModItems.SANS_BONES.get());
        ItemEntity drop = new ItemEntity(entity.level(),
                entity.getX(), entity.getY(), entity.getZ(), stack);
        drop.setDefaultPickUpDelay();
        event.getDrops().add(drop);
    }
}
