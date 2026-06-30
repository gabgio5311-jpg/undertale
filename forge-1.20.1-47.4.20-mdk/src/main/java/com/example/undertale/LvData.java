package com.example.undertale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Sistema de LV (LOVE, estilo Undertale).
 *
 * A "alma" (soul) é a moeda de progresso: cada mob morto com o {@code the_real_knife}
 * vale 1 alma (ver {@link TheRealKnifeItem}). Quanto mais almas, maior o LV.
 *
 * <p>O total de almas é guardado no <b>NBT persistente</b> do jogador
 * ({@link Player#PERSISTED_NBT_TAG}), que o Forge copia automaticamente ao morrer/respawnar
 * — então o progresso não se perde. Essa é a <b>fonte da verdade no servidor</b>; o cliente
 * recebe uma cópia via {@link ModNetwork#syncLv} para mostrar na GUI/tooltip
 * (ver {@link ClientLvData}).
 *
 * <p>Esta classe é <b>comum</b> (sem nada de cliente), então pode ser usada nos dois lados.
 */
public final class LvData {

    private LvData() {}

    /** Chave do total de almas dentro do sub-tag persistente do jogador. */
    private static final String KEY_ALMAS = "undertale_lv_almas";

    /** LV máximo (igual ao Undertale). */
    public static final int MAX_LV = 20;

    /** Resultado já calculado: LV atual + progresso de almas dentro desse LV. */
    public record LvInfo(int lv, int almasAtuais, int almasNecessarias, int total) {
        /** true quando já chegou no LV máximo (barra cheia / "MAX"). */
        public boolean isMax() {
            return lv >= MAX_LV;
        }
    }

    // ---- Fórmula de progresso (tunável) -------------------------------------

    /** Almas necessárias para subir do {@code lv} para {@code lv + 1}. */
    public static int almasParaProximo(int lv) {
        return lv * 5; // LV1->2 = 5, LV2->3 = 10, ... cresce a cada nível
    }

    /** Calcula LV e progresso a partir do total de almas acumulado. */
    public static LvInfo compute(int total) {
        if (total < 0) total = 0;
        int lv = 1;
        int acumulado = 0;
        while (lv < MAX_LV) {
            int necessarias = almasParaProximo(lv);
            if (total >= acumulado + necessarias) {
                acumulado += necessarias;
                lv++;
            } else {
                return new LvInfo(lv, total - acumulado, necessarias, total);
            }
        }
        // No LV máximo não há mais progresso.
        return new LvInfo(MAX_LV, 0, 0, total);
    }

    // ---- Armazenamento no NBT persistente do jogador ------------------------

    /** Sub-tag que o Forge preserva entre mortes/respawns. */
    private static CompoundTag persistente(Player player) {
        CompoundTag root = player.getPersistentData();
        if (root.contains(Player.PERSISTED_NBT_TAG)) {
            return root.getCompound(Player.PERSISTED_NBT_TAG);
        }
        CompoundTag novo = new CompoundTag();
        root.put(Player.PERSISTED_NBT_TAG, novo);
        return novo;
    }

    /** Total de almas acumulado pelo jogador. */
    public static int getTotalAlmas(Player player) {
        return persistente(player).getInt(KEY_ALMAS);
    }

    /** Define o total de almas (usado em comandos/reset, se precisar). */
    public static void setTotalAlmas(Player player, int total) {
        if (total < 0) total = 0;
        persistente(player).putInt(KEY_ALMAS, total);
    }

    /** Soma almas e devolve o novo total. */
    public static int addAlmas(Player player, int quantidade) {
        int total = getTotalAlmas(player) + quantidade;
        setTotalAlmas(player, total);
        return total;
    }

    /** LV/progresso já calculado para o jogador. */
    public static LvInfo getInfo(Player player) {
        return compute(getTotalAlmas(player));
    }
}
