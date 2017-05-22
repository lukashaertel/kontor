package eu.metatools.wepwawet2.components

import eu.metatools.wepwawet2.Lobby

/**
 * Basic reference implementation of [Lobby].
 */
class HashLobby : Lobby by hashMapOf<String, Any>()