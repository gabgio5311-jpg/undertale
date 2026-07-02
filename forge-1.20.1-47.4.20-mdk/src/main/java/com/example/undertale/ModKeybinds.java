package com.example.undertale;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Teclas do mod (lado cliente). A tecla é registrada em {@link ClientModEvents}
 * e lida em {@link BoneArmorSansClient}.
 */
public class ModKeybinds {

    // Categoria padrão "Diversos/Miscellaneous" — já existe e é traduzida pelo jogo,
    // então a tecla SEMPRE aparece num lugar fácil de achar em Opções > Controles.
    public static final String CATEGORY = "key.categories.misc";

    // Nome DIRETO (não é chave de tradução), igual ao mod que funciona — assim
    // aparece com esse texto mesmo sem arquivo de lang. Tecla padrão: G.
    public static final KeyMapping THROW_BONE = new KeyMapping(
            "Throw Sans's Bone",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY);
}
