package com.example.actividadevaluable_pmdm_1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        findViewById<Button>(R.id.button1).setOnClickListener {
            startActivity(Intent(this, TapasActivity::class.java))
            finish() // "atrás" desde tapas no vuelve al login
        }

        findViewById<Button>(R.id.buttonRegistro).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }
}