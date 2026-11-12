package com.sendaplus.iptv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class MainActivity : AppCompatActivity() {
    private var exoPlayer: ExoPlayer? = null
    private var libVlc: LibVLC? = null
    private var vlcPlayer: MediaPlayer? = null

    private lateinit var playerView: PlayerView
    private lateinit var videoContainer: android.widget.FrameLayout
    private lateinit var guidePanel: LinearLayout
    private lateinit var numberView: TextView
    private lateinit var nameView: TextView
    private lateinit var channelInfo: View
    private lateinit var adapter: ChannelAdapter
    private lateinit var vlcVideoLayout: VLCVideoLayout

    private var channels: List<TvChannel> = emptyList()
    private var selected = 0
    private var usingVlc = false
    private var vlcAttached = false
    private var guideVisible = false
    private var activePlaylistId: String? = null
    private var playAttempt = 0
    private var firstVideoFrame = false

    private val handler = Handler(Looper.getMainLooper())
    private var osdRunnable: Runnable? = null
    private var fallbackRunnable: Runnable? = null

    private val openPlaylist =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { importPlaylist(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterImmersiveMode()
        setContentView(R.layout.activity_main)

        videoContainer = findViewById(R.id.video_container)
        playerView = findViewById(R.id.player_view)
        guidePanel = findViewById(R.id.guide_panel)
        numberView = findViewById(R.id.channel_number)
        nameView = findViewById(R.id.channel_name)
        channelInfo = findViewById(R.id.channel_info)

        val list = findViewById<RecyclerView>(R.id.channel_list)
        list.layoutManager = LinearLayoutManager(this)
        adapter = ChannelAdapter { index ->
            playChannel(index)
            setGuideVisible(false)
            videoContainer.requestFocus()
        }
        list.adapter = adapter

        findViewById<Button>(R.id.import_button).setOnClickListener {
            openPlaylist.launch(
                arrayOf(
                    "audio/x-mpegurl",
                    "application/vnd.apple.mpegurl",
                    "application/x-mpegURL",
                    "text/plain",
                    "application/octet-stream"
                )
            )
        }
        findViewById<Button>(R.id.playlists_button).setOnClickListener {
            showPlaylistMenu(it)
        }

        playerView.setOnClickListener { toggleGuide() }
        playerView.isFocusable = true
        playerView.isFocusableInTouchMode = true
        videoContainer.isFocusable = true
        videoContainer.isFocusableInTouchMode = true
        videoContainer.setOnClickListener { toggleGuide() }

        // Mantiene VLC por encima del video Media3, pero debajo del OSD.
        vlcVideoLayout = VLCVideoLayout(this)
        videoContainer.addView(
            vlcVideoLayout,
            1,
            android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        vlcVideoLayout.visibility = View.GONE
        vlcVideoLayout.setOnClickListener { toggleGuide() }

        channelInfo.visibility = View.GONE
        loadInitialPlaylist()
        initializePlayers()
        if (channels.isNotEmpty()) playChannel(selected, showOsd = false)
        videoContainer.requestFocus()
    }

    private fun loadInitialPlaylist() {
        val saved = PlaylistStore.getActive(this)
        if (saved != null) {
            activePlaylistId = saved.id
            channels = saved.channels
        } else {
            channels = M3uManager.defaultChannels()
            activePlaylistId = null
        }
        selected = PlaylistStore.getSelectedChannelIndex(this)
            .coerceIn(0, (channels.size - 1).coerceAtLeast(0))
        adapter.submit(channels)
    }

    private fun importPlaylist(uri: Uri) {
        try {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // La lista ya se guarda como datos locales; no depende del permiso URI.
            }

            val imported = M3uManager.parse(this, uri)
            if (imported.isEmpty()) {
                Toast.makeText(this, "No se encontraron canales válidos", Toast.LENGTH_LONG).show()
                return
            }

            val displayName = uri.lastPathSegment
                ?.substringAfterLast('/')
                ?.substringAfterLast(':')
                ?.takeIf { it.isNotBlank() }
                ?: "Lista ${PlaylistStore.getAll(this).size + 1}"

            val playlist = TvPlaylist(
                id = "playlist_${System.currentTimeMillis()}",
                name = displayName,
                channels = imported
            )
            PlaylistStore.save(this, playlist)
            activePlaylistId = playlist.id
            channels = playlist.channels
            selected = 0
            adapter.submit(channels)
            playChannel(0)
            Toast.makeText(
                this,
                "Lista guardada: ${channels.size} canales",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir la lista: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showPlaylistMenu(anchor: View) {
        val menu = PopupMenu(this, anchor)
        val saved = PlaylistStore.getAll(this)
        menu.menu.add(0, 1, 0, "Canales predeterminados")
        saved.forEachIndexed { index, playlist ->
            menu.menu.add(0, index + 2, index + 1, playlist.name)
        }
        menu.setOnMenuItemClickListener { item ->
            if (item.itemId == 1) {
                activePlaylistId = null
                PlaylistStore.setActive(this, null)
                channels = M3uManager.defaultChannels()
            } else {
                val playlist = saved.getOrNull(item.itemId - 2) ?: return@setOnMenuItemClickListener false
                activePlaylistId = playlist.id
                PlaylistStore.setActive(this, playlist.id)
                channels = playlist.channels
            }
            selected = 0
            adapter.submit(channels)
            if (channels.isNotEmpty()) playChannel(0)
            setGuideVisible(false)
            true
        }
        menu.show()
    }

    private fun initializePlayers() {
        exoPlayer = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    if (!usingVlc) {
                        startVlcFallback("Media3 reportó un error de reproducción")
                    }
                }
            })
            exo.addAnalyticsListener(object : AnalyticsListener {
                override fun onRenderedFirstFrame(
                    eventTime: AnalyticsListener.EventTime,
                    output: Any,
                    renderTimeMs: Long
                ) {
                    runOnUiThread {
                        firstVideoFrame = true
                        fallbackRunnable?.let(handler::removeCallbacks)
                        fallbackRunnable = null
                    }
                }
            })
        }

        try {
            libVlc = LibVLC(
                this,
                arrayListOf(
                    "--network-caching=1500",
                    "--live-caching=1500",
                    "--avcodec-hw=any",
                    "--no-video-title-show"
                )
            )
            vlcPlayer = MediaPlayer(libVlc)
            vlcPlayer?.setEventListener { event ->
                if (event.type == MediaPlayer.Event.EncounteredError) {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "VLC tampoco pudo reproducir este flujo. Verifica URL y códecs.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudo iniciar el motor VLC: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun playChannel(index: Int, showOsd: Boolean = true) {
        if (channels.isEmpty()) return
        selected = index.coerceIn(0, channels.lastIndex)
        PlaylistStore.setSelectedChannelIndex(this, selected)
        val channel = channels[selected]
        numberView.text = channel.number.toString()
        nameView.text = channel.name
        adapter.setSelected(selected)
        if (showOsd) showChannelInfo()

        playAttempt += 1
        val thisAttempt = playAttempt
        firstVideoFrame = false
        fallbackRunnable?.let(handler::removeCallbacks)
        fallbackRunnable = null

        usingVlc = false
        try {
            vlcPlayer?.stop()
        } catch (_: Exception) {
        }
        vlcVideoLayout.visibility = View.GONE
        playerView.visibility = View.VISIBLE

        exoPlayer?.let { exo ->
            exo.stop()
            exo.clearMediaItems()
            exo.setMediaItem(MediaItem.fromUri(channel.url))
            exo.prepare()
            exo.playWhenReady = true
        }

        // Si Media3 reproduce audio pero no entrega ningún fotograma, cambia a VLC.
        fallbackRunnable = Runnable {
            if (playAttempt == thisAttempt && !firstVideoFrame && !usingVlc) {
                startVlcFallback("No llegó el primer fotograma de video")
            }
        }.also { handler.postDelayed(it, 10_000L) }
    }

    private fun startVlcFallback(reason: String) {
        if (usingVlc) return
        val channel = channels.getOrNull(selected) ?: return
        if (vlcPlayer == null || libVlc == null) {
            Toast.makeText(this, "$reason. El motor VLC no está disponible.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            usingVlc = true
            fallbackRunnable?.let(handler::removeCallbacks)
            fallbackRunnable = null
            exoPlayer?.pause()
            playerView.visibility = View.GONE
            vlcVideoLayout.visibility = View.VISIBLE

            if (!vlcAttached) {
                vlcPlayer?.attachViews(vlcVideoLayout, null, false, false)
                vlcAttached = true
            }

            val media = Media(libVlc, Uri.parse(channel.url))
            media.setHWDecoderEnabled(true, false)
            media.addOption(":network-caching=1500")
            media.addOption(":http-reconnect")
            vlcPlayer?.media = media
            media.release()
            vlcPlayer?.play()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al iniciar VLC: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showChannelInfo() {
        channelInfo.visibility = View.VISIBLE
        osdRunnable?.let(handler::removeCallbacks)
        osdRunnable = Runnable {
            channelInfo.visibility = View.GONE
            osdRunnable = null
        }.also { handler.postDelayed(it, 3_000L) }
    }

    private fun toggleGuide() = setGuideVisible(!guideVisible)

    private fun setGuideVisible(visible: Boolean) {
        guideVisible = visible
        guidePanel.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            val recycler = findViewById<RecyclerView>(R.id.channel_list)
            recycler.post {
                recycler.findViewHolderForAdapterPosition(selected)?.itemView?.requestFocus()
                    ?: recycler.requestFocus()
            }
        } else {
            videoContainer.requestFocus()
        }
    }

    private fun enterImmersiveMode() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                if (guideVisible) {
                    setGuideVisible(false)
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                if (!guideVisible) {
                    toggleGuide()
                    return true
                }
            }
            KeyEvent.KEYCODE_CHANNEL_UP -> {
                if (!guideVisible && channels.isNotEmpty()) {
                    playChannel((selected + 1) % channels.size)
                    return true
                }
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                if (!guideVisible && channels.isNotEmpty()) {
                    playChannel((selected - 1 + channels.size) % channels.size)
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (!guideVisible && channels.isNotEmpty()) {
                    playChannel((selected + 1) % channels.size)
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (!guideVisible && channels.isNotEmpty()) {
                    playChannel((selected - 1 + channels.size) % channels.size)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.pause()
        vlcPlayer?.pause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        playerView.player = null
        try {
            vlcPlayer?.stop()
            if (vlcAttached) vlcPlayer?.detachViews()
            vlcPlayer?.release()
            libVlc?.release()
        } catch (_: Exception) {
        }
        exoPlayer?.release()
        vlcPlayer = null
        libVlc = null
        exoPlayer = null
        super.onDestroy()
    }

    private class ChannelAdapter(
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ChannelAdapter.Holder>() {
        private var items: List<TvChannel> = emptyList()
        private var selected = -1

        fun submit(list: List<TvChannel>) {
            items = list
            selected = -1
            notifyDataSetChanged()
        }

        fun setSelected(index: Int) {
            val old = selected
            selected = index
            if (old >= 0) notifyItemChanged(old)
            if (index >= 0 && index < itemCount) notifyItemChanged(index)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
            Holder(
                android.view.LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_channel, parent, false)
            )

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = items[position]
            holder.number.text = item.number.toString()
            holder.name.text = item.name
            holder.itemView.isSelected = position == selected
            holder.itemView.setOnClickListener {
                val current = holder.bindingAdapterPosition
                if (current != RecyclerView.NO_POSITION) onClick(current)
            }
            holder.itemView.setOnFocusChangeListener { view, focused ->
                val current = holder.bindingAdapterPosition
                view.isSelected = focused || current == selected
            }
        }

        class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val number: TextView = view.findViewById(R.id.item_number)
            val name: TextView = view.findViewById(R.id.item_name)
        }
    }
}
