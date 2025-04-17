package com.laundry

import android.content.Intent
import android.os.Bundle
import androidx.cardview.widget.CardView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class login_form : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_form)

        val nextButton: CardView = findViewById(R.id.next)

        nextButton.setOnClickListener{
            val intent = Intent(this, Beranda_laundry::class.java)
            startActivity(intent)
        }

    }
}