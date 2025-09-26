package lavalink.server.util

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

fun loadAudioItem(manager: AudioPlayerManager, identifier: String): AudioItem? {
    val result = AtomicReference<AudioItem?>()
    val exception = AtomicReference<Throwable?>()
    val latch = CountDownLatch(1)
    
    val handler = object : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
            result.set(track)
            latch.countDown()
        }
        
        override fun playlistLoaded(playlist: AudioPlaylist) {
            result.set(playlist)
            latch.countDown()
        }
        
        override fun noMatches() {
            latch.countDown()
        }
        
        override fun loadFailed(ex: FriendlyException) {
            exception.set(ex)
            latch.countDown()
        }
    }
    
    try {
        manager.loadItem(AudioReference(identifier, null), handler)
        latch.await()
        
        exception.get()?.let { throw it }
        return result.get()
        
    } catch (ex: Throwable) {
        ExceptionTools.rethrowErrors(ex)
        throw FriendlyException(
            "Something went wrong while looking up the track.",
            FriendlyException.Severity.FAULT,
            ex
        )
    }
}