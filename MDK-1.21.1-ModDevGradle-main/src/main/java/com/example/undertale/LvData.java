package com.example.undertale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Sistema de LV (LOVE, estilo Undertale).
 *
 * A "alma" (soul) é a moeda de progresso (EXP): cada mob morto com o {@code real_knife}
 * comum rende EXP conforme a categoria do mob (ver {@link LvEvents#onMobKilled}).
 * Quanto mais EXP acumulado, maior o LV.
 *
 * <p>O total de almas é guardado no <b>NBT persistente</b> do jogador
 * ({@link Player#PERSISTED_NBT_TAG}), que o NeoForge copia automaticamente ao morrer/respawnar
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

    // ---- Tabela de progresso (EXP oficial do Undertale) ---------------------

    /**
     * EXP <b>total acumulado</b> necessário para <i>estar</i> em cada LV (índice = LV).
     * Ex.: LV 2 exige 10 EXP no total; LV 3 exige 30; ... LV 20 exige 99999.
     * A posição 0 não é usada (o LV começa em 1). Tabela retirada do Undertale.
     */
    private static final int[] EXP_TOTAL_POR_LV = {
            0,      // (não usado)
            0,      // LV 1
            10,     // LV 2
            30,     // LV 3
            70,     // LV 4
            120,    // LV 5
            200,    // LV 6
            300,    // LV 7
            500,    // LV 8
            800,    // LV 9
            1200,   // LV 10
            1700,   // LV 11
            2500,   // LV 12
            3500,   // LV 13
            5000,   // LV 14
            7000,   // LV 15
            10000,  // LV 16
            15000,  // LV 17
            25000,  // LV 18
            50000,  // LV 19
            99999,  // LV 20
    };

    /** EXP total acumulado necessário para estar no {@code lv} (clampado em 1..MAX_LV). */
    public static int almasParaLv(int lv) {
        if (lv < 1) lv = 1;
        if (lv > MAX_LV) lv = MAX_LV;
        return EXP_TOTAL_POR_LV[lv];
    }

    /** Calcula LV e progresso a partir do total de almas (EXP) acumulado. */
    public static LvInfo compute(int total) {
        if (total < 0) total = 0;

        // Maior LV cujo limiar de EXP já foi alcançado.
        int lv = 1;
        while (lv < MAX_LV && total >= EXP_TOTAL_POR_LV[lv + 1]) {
            lv++;
        }

        if (lv >= MAX_LV) {
            // No LV máximo não há mais progresso.
            return new LvInfo(MAX_LV, 0, 0, total);
        }

        int base = EXP_TOTAL_POR_LV[lv];
        int necessarias = EXP_TOTAL_POR_LV[lv + 1] - base; // EXP deste LV para o próximo
        return new LvInfo(lv, total - base, necessarias, total);
    }

    // ---- Armazenamento no NBT persistente do jogador ------------------------

    /** Sub-tag que o NeoForge preserva entre mortes/respawns. */
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
