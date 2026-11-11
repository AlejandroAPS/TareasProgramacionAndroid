package com.example.actividadevaluable_pmdm_1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etContra: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        etEmail = findViewById(R.id.etEmail)
        etContra = findViewById(R.id.etContra)

        findViewById<Button>(R.id.button1).setOnClickListener { intentarLogin() }

        findViewById<Button>(R.id.buttonRegistro).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        findViewById<TextView>(R.id.tvOlvidoContra).setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                etEmail.error = "Escribe tu correo para recuperar la contraseña"
                etEmail.requestFocus()
            } else {
                // App de prueba: simulamos el envío
                Toast.makeText(this, "Si el correo existe, recibirás instrucciones", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun intentarLogin() {
        etEmail.error = null
        etContra.error = null

        val email = etEmail.text.toString().trim()
        val contra = etContra.text.toString()

        if (email.isEmpty()) {
            etEmail.error = "Introduce tu correo electrónico"
            etEmail.requestFocus()
            return
        }
        if (contra.isEmpty()) {
            etContra.error = "Introduce tu contraseña"
            etContra.requestFocus()
            return
        }
        if (!UsuarioPrefs.hayUsuarioRegistrado(this)) {
            Toast.makeText(this, "No hay ninguna cuenta creada. Regístrate primero", Toast.LENGTH_LONG).show()
            return
        }

        val prefs = UsuarioPrefs.get(this)
        val emailGuardado = prefs.getString(UsuarioPrefs.KEY_EMAIL, "") ?: ""
        val contraGuardada = prefs.getString(UsuarioPrefs.KEY_CONTRA, "") ?: ""

        if (!email.equals(emailGuardado, ignoreCase = true)) {
            etEmail.error = "No existe ninguna cuenta con ese correo"
            etEmail.requestFocus()
            return
        }
        if (contra != contraGuardada) {
            etContra.error = "Contraseña incorrecta"
            etContra.requestFocus()
            return
        }

        val nombre = prefs.getString(UsuarioPrefs.KEY_NOMBRE, "") ?: ""
        Toast.makeText(this, "¡Bienvenido, $nombre!", Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, TapasActivity::class.java))
        finish() // "atrás" desde tapas no vuelve al login
    }
}