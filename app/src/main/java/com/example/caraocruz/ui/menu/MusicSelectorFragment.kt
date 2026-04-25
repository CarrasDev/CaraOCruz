package com.example.caraocruz.ui.menu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentMusicSelectorBinding
import com.example.caraocruz.utils.MusicManager

class MusicSelectorFragment : Fragment(R.layout.fragment_music_selector) {

    private var _binding: FragmentMusicSelectorBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var musicManager: MusicManager

    private val pickAudioLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val ctx = context ?: return@registerForActivityResult
        
        if (uri != null) {
            try {
                val contentResolver = ctx.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // No persistable permissions possible for this provider
            }
            
            musicManager.setCustomBackgroundMusic(uri)
            updateCurrentMusicText()
            Toast.makeText(ctx, getString(R.string.msg_music_updated), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(ctx, getString(R.string.msg_no_file_selected), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMusicSelectorBinding.bind(view)
        
        musicManager = MusicManager.getInstance(requireContext())
        updateCurrentMusicText()

        binding.btnSelectFile.setOnClickListener {
            pickAudioLauncher.launch("audio/*")
        }

        binding.btnResetMusic.setOnClickListener {
            musicManager.setCustomBackgroundMusic(null)
            updateCurrentMusicText()
            Toast.makeText(requireContext(), getString(R.string.msg_music_reset), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCurrentMusicText() {
        if (_binding == null) return
        
        val uriString = musicManager.getCurrentMusicName()
        val displayName = if (uriString != null) {
            getFileName(Uri.parse(uriString))
        } else {
            getString(R.string.default_music)
        }
        
        binding.tvCurrentMusic.text = getString(R.string.current_music, displayName)
    }

    private fun getFileName(uri: Uri): String {
        var name = uri.path ?: "Unknown"
        
        // Intentar obtener el nombre real a través del ContentResolver (mejor para URIs de MediaStore)
        try {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            // Fallback al nombre del path si falla el cursor
            val lastSlash = name.lastIndexOf('/')
            if (lastSlash != -1) {
                name = name.substring(lastSlash + 1)
            }
        }
        
        return name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
