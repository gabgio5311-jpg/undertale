package com.example.undertale;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Núcleo do sistema de LV (LOVE) e sincronização com o cliente.
 *
 * Ganho de EXP por kill, dano escalado do {@code real_knife}, o gate de craft no LV
 * máximo e o re-sync do total nos momentos em que o cliente "começa do zero"
 * (login / respawn / troca de dimensão). O incremento por kill já sincroniza na hora.
 *
 * Bus GAME, auto-registrado (igual a {@code ModEvents}).
 */
@EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class LvEvents {

    /**
     * Conta o EXP do LV: quando um <b>Mob</b> morre por um jogador que está
     * segurando o <b>real_knife comum</b> ({@link ModItems#REAL_KNIFE}).
     *
     * Só essa faca conta — a ideia é farmar LV com ela e, ao chegar no LV máximo,
     * desbloquear o craft da armadura do Sans e do the_real_knife.
     */
    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        // Só no servidor (fonte da verdade) e só pra mobs.
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        // Quem matou precisa ser um jogador segurando o real_knife na mão principal.
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || !player.getMainHandItem().is(ModItems.REAL_KNIFE.get())) {
            return;
        }

        RandomSource rng = player.getRandom();

        // Chefes (Wither, Ender Dragon): sobem 3–4 níveis de uma vez.
        if (mob instanceof WitherBoss || mob instanceof EnderDragon) {
            int lvAtual = LvData.getInfo(player).lv();
            int lvAlvo = Math.min(LvData.MAX_LV, lvAtual + 3 + rng.nextInt(2)); // +3 ou +4
            int expAlvo = LvData.almasParaLv(lvAlvo);
            if (LvData.getTotalAlmas(player) < expAlvo) {
                LvData.setTotalAlmas(player, expAlvo);
            }
        } else {
            LvData.addAlmas(player, expPorMob(mob, rng));
        }

        ModNetwork.syncLv(player); // atualiza GUI/tooltip na hora
    }

    /** EXP concedido por matar um mob comum, conforme a categoria. */
    private static int expPorMob(Mob mob, RandomSource rng) {
        // Monstros perigosos: 50–100.
        if (mob instanceof EnderMan || mob instanceof Witch || mob instanceof PiglinBrute
                || mob instanceof Ravager || mob instanceof Warden) {
            return 50 + rng.nextInt(51);
        }
        // Monstros comuns (hostis em geral): 10–20.
        if (mob instanceof Enemy) {
            return 10 + rng.nextInt(11);
        }
        // Animais passivos / neutros: 1–5.
        return 1 + rng.nextInt(5);
    }

    /** Dano extra do real_knife por nível de LV acima do LV 1 (tunável). */
    public static final float DANO_POR_LV = 5.0F;

    /**
     * O <b>real_knife comum</b> fica mais forte conforme o LV: quanto maior o LV do
     * jogador que ataca, mais dano ele causa.
     *
     * No NeoForge 1.21.1 o antigo {@code LivingHurtEvent} (dano já reduzido pela armadura,
     * mas ainda modificável) virou {@link LivingDamageEvent.Pre}. Aplicamos o bônus aqui
     * para que ele passe DEPOIS da redução de armadura, como o dano normal.
     * O {@code the_real_knife} não é afetado — ele já dá dano infinito.
     */
    @SubscribeEvent
    public static void onRealKnifeHit(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!player.getMainHandItem().is(ModItems.REAL_KNIFE.get())) return;

        int lv = LvData.getInfo(player).lv();
        if (lv <= 1) return; // LV 1: dano base, sem bônus

        float bonus = (lv - 1) * DANO_POR_LV;
        event.setNewDamage(event.getNewDamage() + bonus);
    }

    /** Itens que só podem ser craftados ao chegar no LV máximo. */
    private static boolean isLv20Gated(ItemStack stack) {
        return stack.is(ModItems.THE_REAL_KNIFE.get())
                || stack.is(ModItems.SANS_HELMET.get())
                || stack.is(ModItems.SANS_CHESTPLATE.get())
                || stack.is(ModItems.SANS_LEGGINGS.get())
                || stack.is(ModItems.SANS_BOOTS.get());
    }

    /**
     * Trava o craft da armadura do Sans e do the_real_knife até o jogador chegar no
     * LV máximo ({@link LvData#MAX_LV}).
     *
     * O {@link PlayerEvent.ItemCraftedEvent} dispara ANTES do consumo dos ingredientes,
     * então a grade ainda está cheia: devolvemos 1 item de cada slot ocupado (= exatamente
     * 1 craft) e zeramos o resultado. O item "monta" mas é revertido na hora, sem o
     * jogador perder material.
     */
    @SubscribeEvent
    public static void onGatedCraft(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack result = event.getCrafting();
        if (!isLv20Gated(result)) return;

        // LV suficiente? libera normalmente.
        if (LvData.getInfo(player).lv() >= LvData.MAX_LV) return;

        // Devolve os ingredientes de UM craft (a grade ainda não foi consumida).
        Container grid = event.getInventory();
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack ing = grid.getItem(i);
            if (ing.isEmpty()) continue;
            ItemStack devolver = ing.copyWithCount(1);
            if (!player.getInventory().add(devolver)) {
                player.drop(devolver, false);
            }
        }

        // Cancela o resultado: o jogador não leva o item.
        result.setCount(0);

        player.sendSystemMessage(Component.literal(
                "§4Chara: You don't have enough DETERMINATION yet. Come back at LV " + LvData.MAX_LV + "."));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.syncLv(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.syncLv(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.syncLv(player);
        }
    }
}
