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
 * Guarda listas y canales en SharedPreferences como JSON.
 * Los datos se conservan aunque Android cierre o reinicie la aplicación.
 */
object PlaylistStore {
    private const val PREFS = "senda_playlist_store"
    private const val KEY_PLAYLISTS = "playlists_json"
    private const val KEY_ACTIVE = "active_playlist_id"
    private const val KEY_CHANNEL_INDEX = "active_channel_index"

    fun getAll(context: Context): List<TvPlaylist> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PLAYLISTS, null) ?: return emptyList()

        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val channelsJson = item.optJSONArray("channels") ?: JSONArray()
                    val channels = mutableListOf<TvChannel>()
                    for (j in 0 until channelsJson.length()) {
                        val channel = channelsJson.optJSONObject(j) ?: continue
                        channels += TvChannel(
                            number = channel.optInt("number", 101 + j),
                            name = channel.optString("name", "Canal ${j + 1}"),
                            url = channel.optString("url", ""),
                            group = channel.optString("group", "")
                        )
                    }
                    if (channels.isNotEmpty()) {
                        add(
                            TvPlaylist(
                                id = item.optString("id", "playlist_$i"),
                                name = item.optString("name", "Lista ${i + 1}"),
                                channels = channels
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, playlist: TvPlaylist) {
        val playlists = getAll(context).toMutableList()
        val existing = playlists.indexOfFirst { it.id == playlist.id }
        if (existing >= 0) playlists[existing] = playlist else playlists.add(playlist)
        persist(context, playlists)
        setActive(context, playlist.id)
    }

    fun setActive(context: Context, id: String?) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        if (id == null) editor.remove(KEY_ACTIVE) else editor.putString(KEY_ACTIVE, id)
        editor.apply()
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

    fun getActiveId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ACTIVE, null)

    fun getActive(context: Context): TvPlaylist? {
        val playlists = getAll(context)
        val activeId = getActiveId(context)
        return playlists.firstOrNull { it.id == activeId } ?: playlists.lastOrNull()
    }

    private fun persist(context: Context, playlists: List<TvPlaylist>) {
        val array = JSONArray()
        playlists.forEach { playlist ->
            val item = JSONObject()
                .put("id", playlist.id)
                .put("name", playlist.name)
            val channels = JSONArray()
            playlist.channels.forEach { channel ->
                channels.put(
                    JSONObject()
                        .put("number", channel.number)
                        .put("name", channel.name)
                        .put("url", channel.url)
                        .put("group", channel.group)
                )
            }
            item.put("channels", channels)
            array.put(item)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PLAYLISTS, array.toString())
            .apply()
    }
}
