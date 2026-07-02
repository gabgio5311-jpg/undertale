package com.example.undertale;

import net.minecraft.advancements.CriteriaTriggers;

/**
 * Triggers de advancement customizados do mod.
 *
 * {@link CriteriaTriggers#register} mexe num mapa estático compartilhado, então
 * {@link #register()} deve ser chamado no setup comum, dentro de
 * {@code enqueueWork} (ver {@link UndertaleMod}).
 */
public final class ModCriteria {

    private ModCriteria() {}

    /** Dispara ao alcançar um LV mínimo (usado pelas conquistas de LV). */
    public static final ReachLvTrigger REACH_LV = new ReachLvTrigger();

    public static void register() {
        CriteriaTriggers.register(REACH_LV);
    }
}
