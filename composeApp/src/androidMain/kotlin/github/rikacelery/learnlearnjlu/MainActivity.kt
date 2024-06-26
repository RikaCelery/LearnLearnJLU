package github.rikacelery.learnlearnjlu

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import service.DB
import ui.App
import java.io.File

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var INSTANCE: MainActivity
    }

    fun saveFile(file: File) {
        // Add a specific media item.
        val resolver = applicationContext.contentResolver

// Find all audio files on the primary external storage device.
        val audioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

// Publish a new song.
        val newSongDetails = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
        }

// Keep a handle to the new song's URI in case you need to modify it
// later.
        val songContentUri = resolver
            .insert(audioCollection, newSongDetails)!!

        // "w" for write.
        contentResolver.openOutputStream(songContentUri)?.use {
            file.inputStream().use { input ->
                input.copyTo(it)
            }
        }
        file.delete()


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        INSTANCE = this
        try {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                "test.mp4"
            ).createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        DB.init(this)
        setContent {
            App()
        }
    }
}

@Preview
@Composable
@Suppress("Unused")
fun AppAndroidPreview() {
    App()
}