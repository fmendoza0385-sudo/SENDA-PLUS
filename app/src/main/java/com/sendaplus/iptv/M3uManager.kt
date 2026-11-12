package com.sendaplus.iptv

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

data class TvChannel(
    val number: Int,
    val name: String,
    val url: String,
    val group: String = ""
)

object M3uManager {
    private val defaults = listOf(
        TvChannel(101, "Canal 1", "http://190.109.2.92:8000/play/a010/index.m3u8", "Nacionales"),
        TvChannel(102, "Caracol HD2", "http://181.78.14.26:4000/play/a00d/index.m3u8", "Nacionales"),
        TvChannel(
            103,
            "Red+",
            "https://inforedvos.lcdn.claro.net.co/Content/HLS_HLS_DIR/Live/channel(REDMASHDWEB)/master.m3u8",
            "Noticias y Entretenimiento"
        )
    )

    fun defaultChannels(): List<TvChannel> = defaults

    fun parse(context: Context, uri: Uri): List<TvChannel> {
        val input = context.contentResolver.openInputStream(uri) ?: return emptyList()
        val result = mutableListOf<TvChannel>()
        var name = "Canal"
        var group = ""

        input.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                reader.forEachLine { rawLine ->
                    val value = rawLine.trim().removePrefix("\uFEFF")
                    when {
                        value.startsWith("#EXTINF", ignoreCase = true) -> {
                            // El nombre se encuentra después de la última coma.
                            name = value.substringAfterLast(',', "Canal").trim()
                                .ifBlank { "Canal ${result.size + 1}" }
                            group = Regex("""group-title="([^"]*)"""")
                                .find(value)?.groupValues?.getOrNull(1).orEmpty()
                        }
                        value.isNotBlank() && !value.startsWith("#") &&
                            (value.startsWith("http://", true) ||
                             value.startsWith("https://", true)) -> {
                            result += TvChannel(
                                number = 101 + result.size,
                                name = name,
                                url = value,
                                group = group
                            )
                            name = "Canal"
                            group = ""
                        }
                    }
                }
            }
        }
        return result
    }
}
