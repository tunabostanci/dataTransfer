package com.example.filetransfer

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import com.example.filetransfer.R
import android.content.ContentResolver

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            Log.d("PhotoCheck", "Seçilen dosya URI: $uri")
            viewModel.sendFile(uri, requireContext().contentResolver)
        } else {
            Toast.makeText(requireContext(), "Dosya seçilmedi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val sendBtn = view.findViewById<Button>(R.id.sendBtn)
        val receiveBtn = view.findViewById<Button>(R.id.receiveBtn)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        progressBar.max = 100
        progressBar.progress = 0

        // Upload progress gözlemleme
        viewModel.uploadProgress.observe(viewLifecycleOwner) { progress ->
            progressBar.progress = progress
            Log.d("UploadProgress", "Progress: $progress%")
        }

        // Upload durumu gözlemleme
        viewModel.uploadStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
            Log.d("UploadStatus", status)
        }

        sendBtn.setOnClickListener {
            filePicker.launch("*/*") // Tüm dosyalar, istersen "image/*" da olabilir
        }

        receiveBtn.setOnClickListener {
            // Dosya indirme işlemi buraya
            Toast.makeText(requireContext(), "Receive fonksiyonu henüz yok", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
