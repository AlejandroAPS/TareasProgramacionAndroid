package com.example.actividadevaluable_pmdm_1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UsuarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuario) // usa el nombre real de tu layout

        val prefs = UsuarioPrefs.get(this)

        fun dato(clave: String) = prefs.getString(clave, "") ?: ""

        findViewById<TextView>(R.id.tvNombreUsuario).text =
            "Nombre: ${dato(UsuarioPrefs.KEY_NOMBRE)}"
        findViewById<TextView>(R.id.tvApellidosUsuario).text =
            "Apellidos: ${dato(UsuarioPrefs.KEY_APELLIDOS)}"
        findViewById<TextView>(R.id.tvEmailUsuario).text =
            "Email: ${dato(UsuarioPrefs.KEY_EMAIL)}"
        findViewById<TextView>(R.id.tvDireccionUsuario).text =
            "Dirección: ${dato(UsuarioPrefs.KEY_DIRECCION)}"
        findViewById<TextView>(R.id.tvCodigoPostalUsuario).text =
            "Código Postal: ${dato(UsuarioPrefs.KEY_CODIGO_POSTAL)}"

        findViewById<Button>(R.id.buttonCerrarSesion).setOnClickListener {
            // Vaciamos la pila para que "atrás" no devuelva a la app.
            // No tocamos las SharedPreferences: la cuenta sigue existiendo.
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        findViewById<Button>(R.id.buttonVolver).setOnClickListener {
            finish()
        }
    }
}