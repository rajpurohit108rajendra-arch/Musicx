package com.musicx.app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

private val Green = Color(0xFF1ED760)
private val Bg = Color(0xFF121212)
private val Panel = Color(0xFF242424)
private val Muted = Color(0xFFB3B3B3)

data class Track(val title: String, val artist: String, val url: String, val isLocal: Boolean = false)

private val tracks = listOf(
    Track("SoundHelix Song 1", "SoundHelix • Demo track",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
    Track("SoundHelix Song 2", "SoundHelix • Demo track",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"),
    Track("SoundHelix Song 3", "SoundHelix • Demo track",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3")
)

class MainActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        setContent {
            MusicXApp(
                context = this,
                player = player,
                onClose = { if (player.isPlaying) player.pause() }
            )
        }
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}

@Composable
private fun MusicXApp(context: Context, player: ExoPlayer, onClose: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var current by remember { mutableStateOf<Track?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf(setOf<String>()) }
    var showFavorites by remember { mutableStateOf(false) }
    var onlineUrl by remember { mutableStateOf("") }
    val addedTracks = remember { mutableStateListOf<Track>() }
    val pickAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            val title = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')?.takeIf { it.isNotBlank() } ?: "Phone song"
            if (addedTracks.none { it.url == uri.toString() }) {
                runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                addedTracks.add(Track(title, "Phone • Local audio", uri.toString(), true))
            }
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    val allTracks = tracks + addedTracks
    val filtered = allTracks.filter {
        (it.title.contains(query, true) || it.artist.contains(query, true)) &&
            (!showFavorites || favorites.contains(it.title))
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Green, background = Bg, surface = Panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Column(
            Modifier.fillMaxSize().background(Bg).padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GraphicEq, null, tint = Green, modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(8.dp))
                Text("MusicX", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Green)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showFavorites = !showFavorites }) {
                    Icon(Icons.Default.Favorite, "Show playlist", tint = if (showFavorites) Green else Color.White)
                }
            }
            Text("Your music. Your vibe.", color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { pickAudio.launch(arrayOf("audio/*")) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FolderOpen, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add phone songs")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = onlineUrl, onValueChange = { onlineUrl = it }, singleLine = true,
                    modifier = Modifier.weight(1f).background(Panel, RoundedCornerShape(12.dp)).padding(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                    decorationBox = { inner -> if (onlineUrl.isEmpty()) Text("Paste direct audio URL (.mp3 etc.)", color = Muted, fontSize = 12.sp); inner() }
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val u = onlineUrl.trim()
                    if ((u.startsWith("https://") || u.startsWith("http://")) && u.length > 12) {
                        val name = u.substringBefore('?').substringAfterLast('/').ifBlank { "Online stream" }
                        addedTracks.add(Track(name, "Online • Added URL", u, false))
                        onlineUrl = ""
                        android.widget.Toast.makeText(context, "Online stream added", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Paste a direct http(s) audio URL", android.widget.Toast.LENGTH_LONG).show()
                    }
                }) { Text("Add") }
            }
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(28.dp)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = Muted)
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = query, onValueChange = { query = it },
                    singleLine = true, modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("Search songs or artists", color = Muted)
                        inner()
                    }
                )
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Close, "Clear search", tint = Muted)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Text(if (showFavorites) "YOUR PLAYLIST" else "YOUR MUSIC",
                fontSize = 13.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, color = Muted)
            Spacer(Modifier.height(10.dp))

            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered, key = { it.title }) { track ->
                    Row(
                        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(52.dp).background(
                                listOf(Color(0xFF4C2A85), Color(0xFF165B45), Color(0xFF8A3B21))[allTracks.indexOf(track).coerceAtLeast(0) % 3],
                                RoundedCornerShape(10.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(track.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Spacer(Modifier.height(3.dp))
                            Text(track.artist, color = Muted, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            if (current?.title == track.title && player.isPlaying) {
                                player.pause()
                            } else {
                                if (current?.title != track.title) {
                                    current = track
                                    player.setMediaItem(MediaItem.fromUri(track.url))
                                    player.prepare()
                                }
                                player.play()
                            }
                        }) {
                            Icon(
                                if (current?.title == track.title && isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = "Play ${track.title}", tint = Green, modifier = Modifier.size(34.dp)
                            )
                        }
                        if (!track.isLocal) {
                            IconButton(onClick = { downloadTrack(context, track) }) {
                                Icon(Icons.Default.Download, contentDescription = "Download ${track.title}", tint = Color.White)
                            }
                        }
                        IconButton(onClick = {
                            favorites = if (favorites.contains(track.title)) favorites - track.title else favorites + track.title
                        }) {
                            Icon(Icons.Default.Favorite, contentDescription = "Add to playlist",
                                tint = if (favorites.contains(track.title)) Green else Muted)
                        }
                    }
                }
                if (filtered.isEmpty()) {
                    item { Text("No songs found. Try another search or add tracks to your playlist.", color = Muted) }
                }
            }

            current?.let { track ->
                Row(
                    Modifier.fillMaxWidth().background(Color(0xFF202020), RoundedCornerShape(14.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("NOW PLAYING", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(track.title, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = {
                        if (player.isPlaying) player.pause() else player.play()
                    }) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Pause or resume", tint = Green, modifier = Modifier.size(32.dp))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Phone songs: tap Add phone songs. Online: add a direct audio stream URL. Only save music you have permission to download.",
                color = Muted, fontSize = 10.sp, modifier = Modifier.padding(bottom = 12.dp))
        }
    }
}

private fun downloadTrack(context: Context, track: Track) {
    try {
        val request = DownloadManager.Request(Uri.parse(track.url))
            .setTitle(track.title)
            .setDescription("Downloading with MusicX")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeFileName(track.title) + ".mp3")
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        android.widget.Toast.makeText(context, "Download started", android.widget.Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Could not start download", android.widget.Toast.LENGTH_LONG).show()
    }
}

private fun safeFileName(value: String): String =
    value.replace(Regex("[^A-Za-z0-9._ -]"), "_").take(80)
