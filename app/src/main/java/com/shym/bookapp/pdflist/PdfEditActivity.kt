package com.shym.bookapp.pdflist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shym.bookapp.R
import com.shym.bookapp.databinding.ActivityPdfEditBinding

class PdfEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfEditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}