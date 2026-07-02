package com.example.undertale;

/**
 * Cache client-side do LV do jogador local.
 *
 * O total de almas vive no servidor (NBT persistente, ver {@link LvData}) e <b>não</b> é
 * sincronizado automaticamente, então o servidor manda uma cópia via
 * {@link ModNetwork#syncLv} sempre que muda. A GUI ({@link IndicadorLvScreen}) e o tooltip
 * ({@link IndicadorLvItem}) leem daqui.
 *
 * <p>Classe propositalmente <b>pura</b> (sem imports de cliente), então é segura de
 * referenciar a partir de código comum — num servidor dedicado ela só fica com 0.
 */
public final class ClientLvData {

    private ClientLvData() {}

    /** Total de almas do jogador local (atualizado pelo pacote de sync). */
    public static int total = 0;

    /** LV/progresso já calculado a partir do total atual. */
    public static LvData.LvInfo info() {
        return LvData.compute(total);
    }
}
