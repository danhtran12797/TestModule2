package com.example.testmodule2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.intelnet.omniwallet.ui.intro.IntroActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart=findViewById(R.id.btnStart)

        btnStart.setOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
        }
    }
}