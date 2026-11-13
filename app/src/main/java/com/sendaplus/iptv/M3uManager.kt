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

/**
 * Lista fija de SENDA PLUS.
 * Los canales se empaquetan en la APK y comienzan en el número 100.
 * No se ofrece importación ni selección de listas alternativas desde la interfaz.
 */
object M3uManager {
    private val fixedChannels = listOf(
        TvChannel(100, "Canal Caracol", "http://181.78.197.59:8000/play/a09p/index.m3u8", ""),
        TvChannel(101, "Canal RCN", "http://181.78.197.59:8000/play/a09q/index.m3u8", ""),
        TvChannel(102, "CARACOL INTERNACIONAL", "http://181.78.197.59:8000/play/a06z/index.m3u8", ""),
        TvChannel(103, "CARACOL HD", "http://181.118.156.46:8000/play/a060/index.m3u8", ""),
        TvChannel(104, "5.CANAL CAPITAL", "http://181.118.156.46:8000/play/a05v/index.m3u8", ""),
        TvChannel(105, "5.NOTICIAS NTN 24", "http://181.118.156.46:8000/play/a05z/index.m3u8", ""),
        TvChannel(106, "TELEMEDELLIN", "http://181.118.156.46:8000/play/a04a/index.m3u8", ""),
        TvChannel(107, "caracol", "http://181.78.197.59:8000/play/caracol/index.m3u8", ""),
        TvChannel(108, "Baby TV", "http://181.78.197.59:8000/play/a075/index.m3u8", ""),
        TvChannel(109, "CARTOON NETWORK HD", "http://181.78.197.59:8000/play/a07c/index.m3u8", ""),
        TvChannel(110, "5.DISNEY JR", "http://181.118.156.46:8000/play/a061/index.m3u8", ""),
        TvChannel(111, "5.NICKELODEON COLOMBIA", "http://181.118.156.46:8000/play/a063/index.m3u8", ""),
        TvChannel(112, "CARTUNITO", "http://181.78.197.59:8000/play/a07j/index.m3u8", ""),
        TvChannel(113, "CARTOONITO", "http://181.78.197.59:8000/play/a08g/index.m3u8", ""),
        TvChannel(114, "Discovery Kids HD", "http://181.78.197.59:8000/play/a078/index.m3u8", ""),
        TvChannel(115, "Disney JR", "http://181.78.197.59:8000/play/a07k/index.m3u8", ""),
        TvChannel(116, "Disney Jr.", "http://181.78.197.59:8000/play/a09f/index.m3u8", ""),
        TvChannel(117, "DISCOVERY", "http://181.118.156.46:8000/play/a05s/index.m3u8", ""),
        TvChannel(118, "Animal Planet HD", "http://181.78.197.59:8000/play/a06u/index.m3u8", ""),
        TvChannel(119, "DISCOVERY THEATHER HD", "http://181.78.197.59:8000/play/a07v/index.m3u8", ""),
        TvChannel(120, "Discovery Channel HD", "http://181.78.197.59:8000/play/a086/index.m3u8", ""),
        TvChannel(121, "Discovery ID HD", "http://181.78.197.59:8000/play/a08a/index.m3u8", ""),
        TvChannel(122, "Discovery World HD", "http://181.78.197.59:8000/play/a087/index.m3u8", ""),
        TvChannel(123, "History HD", "http://181.78.197.59:8000/play/a07m/index.m3u8", ""),
        TvChannel(124, "Home & Health HD", "http://181.78.197.59:8000/play/a07w/index.m3u8", ""),
        TvChannel(125, "Nat Geo HD", "http://181.78.197.59:8000/play/a083/index.m3u8", ""),
        TvChannel(126, "5.EWTN (Catolico)", "http://181.118.156.46:8000/play/a05x/index.m3u8", ""),
        TvChannel(127, "ESPN HD", "http://181.78.197.59:8000/play/a0ff/index.m3u8", ""),
        TvChannel(128, "ESPN 2 HD", "http://181.78.197.59:8000/play/a080/index.m3u8", ""),
        TvChannel(129, "ESPN 3 HD", "http://181.78.197.59:8000/play/a081/index.m3u8", ""),
        TvChannel(130, "ESPN  4 HD", "http://181.78.197.59:8000/play/a07n/index.m3u8", ""),
        TvChannel(131, "ESPN 5  HD", "http://181.78.197.59:8000/play/a074/index.m3u8", ""),
        TvChannel(132, "ESPN 6 HD", "http://181.78.197.59:8000/play/a08c/index.m3u8", ""),
        TvChannel(133, "ESPN 7 HD", "http://181.78.197.59:8000/play/a06a/index.m3u8", ""),
        TvChannel(134, "ESPN Premium HD", "http://181.78.197.59:8000/play/a09l/index.m3u8", ""),
        TvChannel(135, "Canal 36", "http://181.118.156.46:8000/play/a05w/index.m3u8", ""),
        TvChannel(136, "TYC SPORTS", "http://181.118.156.46:8000/play/a06a/index.m3u8", ""),
        TvChannel(137, "KDrama HD", "http://181.78.197.59:8000/play/a07x/index.m3u8", ""),
        TvChannel(138, "Las Estrellas HD", "http://181.78.197.59:8000/play/a09j/index.m3u8", ""),
        TvChannel(139, "Pasiones HD", "http://181.78.197.59:8000/play/a088/index.m3u8", ""),
        TvChannel(140, "TVChile", "http://181.78.197.59:8000/play/a076/index.m3u8", ""),
        TvChannel(141, "Teleamazonas", "http://181.78.197.59:8000/play/a09t/index.m3u8", ""),
        TvChannel(142, "Teleamazonas", "http://181.78.197.59:8000/play/a062/index.m3u8", ""),
        TvChannel(143, "Telefe", "http://181.78.197.59:8000/play/a070/index.m3u8", ""),
        TvChannel(144, "Telemundo", "http://181.78.197.59:8000/play/a08x/index.m3u8", ""),
        TvChannel(145, "Telemundo", "http://181.78.197.59:8000/play/a09g/index.m3u8", ""),
        TvChannel(146, "Telemundo HD", "http://181.78.197.59:8000/play/a06x/index.m3u8", ""),
        TvChannel(147, "Televicentro", "http://181.78.197.59:8000/play/a03v/index.m3u8", ""),
        TvChannel(148, "Tlnovelas", "http://181.78.197.59:8000/play/a09k/index.m3u8", ""),
        TvChannel(149, "5.SONY CHANNEL", "http://181.118.156.46:8000/play/a05u/index.m3u8", ""),
        TvChannel(150, "A&E HD", "http://181.78.197.59:8000/play/a06v/index.m3u8", ""),
        TvChannel(151, "AMC HD", "http://181.78.197.59:8000/play/a06w/index.m3u8", ""),
        TvChannel(152, "AXN HD", "http://181.78.197.59:8000/play/a072/index.m3u8", ""),
        TvChannel(153, "FMH MOVIES", "http://181.118.156.46:8000/play/a067/index.m3u8", ""),
        TvChannel(154, "CINECANAL HD", "http://181.78.197.59:8000/play/a07r/index.m3u8#EXTINF:0,", ""),
        TvChannel(155, "Canal 56", "http://181.78.197.59:8000/play/a084/index.m3u8", ""),
        TvChannel(156, "FX HD", "http://181.78.197.59:8000/play/a06y/index.m3u8", ""),
        TvChannel(157, "Golden Premiere 2 HD", "http://181.78.197.59:8000/play/a07a/index.m3u8", ""),
        TvChannel(158, "Golden Premiere HD", "http://181.78.197.59:8000/play/a079/index.m3u8", ""),
        TvChannel(159, "HBO Family HD", "http://181.78.197.59:8000/play/a07g/index.m3u8", ""),
        TvChannel(160, "HBO HD", "http://181.78.197.59:8000/play/a07d/index.m3u8", ""),
        TvChannel(161, "HBO Plus HD", "http://181.78.197.59:8000/play/a07f/index.m3u8", ""),
        TvChannel(162, "HBO XTREME", "http://181.78.197.59:8000/play/a07h/index.m3u8", ""),
        TvChannel(163, "HBO2 HD", "http://181.78.197.59:8000/play/a07e/index.m3u8", ""),
        TvChannel(164, "SONY HD", "http://181.78.197.59:8000/play/a07q/index.m3u8", ""),
        TvChannel(165, "SPACE HD", "http://181.78.197.59:8000/play/a09e/index.m3u8", ""),
        TvChannel(166, "STAR CHANNEL HD", "http://181.78.197.59:8000/play/a07p/index.m3u8", ""),
        TvChannel(167, "Studio Universal HD", "http://181.78.197.59:8000/play/a06t/index.m3u8", ""),
        TvChannel(168, "TNT HD", "http://181.78.197.59:8000/play/a07u/index.m3u8", ""),
        TvChannel(169, "TNT Series HD", "http://181.78.197.59:8000/play/a085/index.m3u8", ""),
        TvChannel(170, "Universal", "http://181.78.197.59:8000/play/a071/index.m3u8", ""),
        TvChannel(171, "Univision HD", "http://181.78.197.59:8000/play/a089/index.m3u8", ""),
        TvChannel(172, "WARNER HD", "http://181.78.197.59:8000/play/a07s/index.m3u8", ""),
        TvChannel(173, "antena", "http://181.78.197.59:8000/play/antena/index.m3u8", ""),
        TvChannel(174, "CineMax#EXTINF:0,5.ADULT SWING", "http://181.78.197.59:8000/play/a08f/index.m3u8http://181.118.156.46:8000/play/a05t/index.m3u8", ""),
        TvChannel(175, "CINE LATINO#EXTINF:0,5.COMEDY CENTRAL", "http://181.78.197.59:8000/play/a0fe/index.m3u8http://181.118.156.46:8000/play/a062/index.m3u8", ""),
        TvChannel(176, "CNNE", "http://181.78.197.59:8000/play/a08s/index.m3u8", ""),
        TvChannel(177, "Canal 78", "http://181.78.197.59:8000/play/a0ed/index.m3u8", ""),
        TvChannel(178, "Ecuavisa", "http://181.78.197.59:8000/play/a09s/index.m3u8", ""),
        TvChannel(179, "RTS", "http://181.78.197.59:8000/play/a03b/index.m3u8", ""),
        TvChannel(180, "RTU", "http://181.78.197.59:8000/play/a0fb/index.m3u8", ""),
        TvChannel(181, "TC", "http://181.78.197.59:8000/play/a09r/index.m3u8", ""),
        TvChannel(182, "TC Televisiðn", "http://181.78.197.59:8000/play/a04i/index.m3u8", ""),
        TvChannel(183, "MEMORIAS TOONS", "http://181.118.156.46:8000/play/a069/index.m3u8", "")
    )

    fun defaultChannels(): List<TvChannel> = fixedChannels

    // Se conserva el parser por compatibilidad interna, numerando siempre desde 100.
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
                            name = value.substringAfterLast(',', "Canal").trim()
                                .ifBlank { "Canal ${result.size + 1}" }
                            group = Regex("""group-title="([^"]*)"""")
                                .find(value)?.groupValues?.getOrNull(1).orEmpty()
                        }
                        value.isNotBlank() && !value.startsWith("#") &&
                            (value.startsWith("http://", true) ||
                             value.startsWith("https://", true)) -> {
                            result += TvChannel(
                                number = 100 + result.size,
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
