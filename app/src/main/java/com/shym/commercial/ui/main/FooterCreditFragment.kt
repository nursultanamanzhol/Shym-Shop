package com.shym.commercial.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.shym.commercial.R

class FooterCreditFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_footer_credit, container, false);
        var footer_website_icon: ImageView = view.findViewById(R.id.footer_website_icon)
        var footer_phone_icon: ImageView = view.findViewById(R.id.footer_phone_icon)
        var footer_email_icon: ImageView = view.findViewById(R.id.footer_email_icon)

        footer_website_icon.setOnClickListener(View.OnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nursultanamanzhol"))
            startActivity(browserIntent)
        })
        footer_phone_icon.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+77075511767")
            startActivity(intent)
        })
        footer_email_icon.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("03nursultans@gmail.com")
            startActivity(intent);
        })

        return view
    }
}