package com.sendaplus.iptv

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class TvPlaylist(
    val id: String,
    val name: String,
    val channels: List<TvChannel>
)

/**
 * Conserva únicamente el índice del último canal visto.
 * No restaura listas importadas previamente: la app usa la lista fija incluida.
 */
object PlaylistStore {
    private const val PREFS = "senda_playlist_store"
    private const val KEY_CHANNEL_INDEX = "active_channel_index"

    fun getAll(context: Context): List<TvPlaylist> = emptyList()

    fun save(context: Context, playlist: TvPlaylist) {
        // Deshabilitado: SENDA PLUS utiliza una única lista M3U8 empaquetada.
    }

    fun setActive(context: Context, id: String?) {
        // Deshabilitado: no hay listas alternativas.
    }

    fun setSelectedChannelIndex(context: Context, index: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CHANNEL_INDEX, index.coerceAtLeast(0))
            .apply()
    }

    fun getSelectedChannelIndex(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_CHANNEL_INDEX, 0)

    fun getActiveId(context: Context): String? = null

    fun getActive(context: Context): TvPlaylist? = null
}
