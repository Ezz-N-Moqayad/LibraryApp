package com.example.libraryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class VideoActivity : AppCompatActivity(), Player.Listener {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar

    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        progressBar = findViewById(R.id.progressBar)
        playerView = findViewById(R.id.video_view)

        database = Firebase.database.reference

        setupPlayer()
        setMP4File(intent.getStringExtra("Uri_Video").toString())

        if (savedInstanceState != null) {
            val seekTime = savedInstanceState.getLong("SeekTime")
            player.seekTo(seekTime)
            player.play()
        }
    }

    private fun setMP4File(Uri_Video: String) {
        val mediaItem = MediaItem.fromUri(Uri_Video)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        player.addListener(this)
    }

    override fun onPlaybackStateChanged(state: Int) {
        when (state) {
            Player.STATE_BUFFERING -> {
                progressBar.visibility = View.VISIBLE
            }
            Player.STATE_READY -> {
                progressBar.visibility = View.INVISIBLE
            }
            Player.STATE_ENDED -> {}
            Player.STATE_IDLE -> {}
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("SeekTime", player.currentPosition)
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.Back -> startActivity(Intent(this, ViewBooks::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}