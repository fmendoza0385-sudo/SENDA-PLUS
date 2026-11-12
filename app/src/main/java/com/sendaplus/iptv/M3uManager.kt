package com.sendaplus.iptv

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

data class TvChannel(val number: Int, val name: String, val url: String, val group: String = "")

object M3uManager {
    private val defaults = listOf(
        TvChannel(101, "Canal 1", "http://190.109.2.92:8000/play/a010/index.m3u8", "Nacionales"),
        TvChannel(102, "Caracol HD2", "http://181.78.14.26:4000/play/a00d/index.m3u8", "Nacionales"),
        TvChannel(103, "Red+", "https://inforedvos.lcdn.claro.net.co/Content/HLS_HLS_DIR/Live/channel(REDMASHDWEB)/master.m3u8", "Noticias y Entretenimiento")
    )

    fun defaultChannels(): List<TvChannel> = defaults

    fun parse(context: Context, uri: Uri): List<TvChannel> {
        val lines = context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).readLines()
        } ?: return emptyList()
        val result = mutableListOf<TvChannel>()
        var name = "Canal"
        var group = ""
        for (line in lines) {
            val value = line.trim()
            if (value.startsWith("#EXTINF", true)) {
                name = value.substringAfterLast(',').ifBlank { "Canal" }
                group = Regex("group-title=\"([^\"]*)\"").find(value)?.groupValues?.getOrNull(1).orEmpty()
            } else if (value.isNotEmpty() && !value.startsWith("#") && (value.startsWith("http://") || value.startsWith("https://"))) {
                result += TvChannel(101 + result.size, name, value, group)
            }
        }
        return result
    }
}
