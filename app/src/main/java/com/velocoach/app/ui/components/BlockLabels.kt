package com.velocoach.app.ui.components

import com.velocoach.app.data.BlockType

fun labelFor(type: BlockType): String = when (type) {
    BlockType.WARMUP -> "Échauffement"
    BlockType.STEADY -> "Continu"
    BlockType.INTERVAL -> "Intervalles"
    BlockType.RECOVERY -> "Récupération"
    BlockType.COOLDOWN -> "Retour au calme"
}
