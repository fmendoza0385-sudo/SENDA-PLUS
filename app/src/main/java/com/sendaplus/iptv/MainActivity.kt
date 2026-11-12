package com.sendaplus.iptv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var numberView: TextView
    private lateinit var nameView: TextView
    private lateinit var adapter: ChannelAdapter
    private var channels: List<TvChannel> = emptyList()
    private var selected = 0

    private val openPlaylist = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val imported = M3uManager.parse(this, it)
                if (imported.isNotEmpty()) {
                    channels = imported
                    selected = 0
                    adapter.submit(channels)
                    playChannel(0)
                    Toast.makeText(this, "Lista cargada: ${channels.size} canales", Toast.LENGTH_LONG).show()
                } else Toast.makeText(this, "No se encontraron canales válidos", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo abrir la lista: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.player_view)
        numberView = findViewById(R.id.channel_number)
        nameView = findViewById(R.id.channel_name)
        val list = findViewById<RecyclerView>(R.id.channel_list)
        list.layoutManager = LinearLayoutManager(this)
        adapter = ChannelAdapter { index -> playChannel(index) }
        list.adapter = adapter
        findViewById<Button>(R.id.import_button).setOnClickListener { openPlaylist.launch(arrayOf("audio/x-mpegurl", "application/vnd.apple.mpegurl", "text/plain", "application/octet-stream")) }
        channels = M3uManager.defaultChannels()
        adapter.submit(channels)
        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Toast.makeText(this@MainActivity, "Error al reproducir el canal", Toast.LENGTH_SHORT).show()
                }
            })
        }
        playChannel(0)
    }

    private fun playChannel(index: Int) {
        if (channels.isEmpty()) return
        selected = index.coerceIn(0, channels.lastIndex)
        val channel = channels[selected]
        numberView.text = channel.number.toString()
        nameView.text = channel.name
        player?.setMediaItem(MediaItem.fromUri(channel.url))
        player?.prepare()
        player?.playWhenReady = true
        adapter.setSelected(selected)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_DPAD_UP -> { playChannel((selected + 1).coerceAtMost(channels.lastIndex)); return true }
            KeyEvent.KEYCODE_CHANNEL_DOWN, KeyEvent.KEYCODE_DPAD_DOWN -> { playChannel((selected - 1).coerceAtLeast(0)); return true }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() { super.onStop(); player?.pause() }
    override fun onDestroy() { playerView.player = null; player?.release(); player = null; super.onDestroy() }

    private class ChannelAdapter(private val onClick: (Int) -> Unit) : RecyclerView.Adapter<ChannelAdapter.Holder>() {
        private var items: List<TvChannel> = emptyList()
        private var selected = -1
        fun submit(list: List<TvChannel>) { items = list; notifyDataSetChanged() }
        fun setSelected(index: Int) { val old = selected; selected = index; if (old >= 0) notifyItemChanged(old); notifyItemChanged(index) }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false))
        override fun getItemCount() = items.size
        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = items[position]
            holder.number.text = item.number.toString()
            holder.name.text = item.name
            holder.itemView.isSelected = position == selected
            holder.itemView.setOnClickListener { onClick(position) }
            holder.itemView.setOnFocusChangeListener { view, focused -> view.isSelected = focused || position == selected }
        }
        class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val number: TextView = view.findViewById(R.id.item_number)
            val name: TextView = view.findViewById(R.id.item_name)
        }
    }
}
