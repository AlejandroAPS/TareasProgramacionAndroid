package com.example.actividadevaluable_pmdm_1

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContra: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etCodigoPostal: EditText
    private lateinit var btnCrearCuenta: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        etNombre = findViewById(R.id.etNombre)
        etApellidos = findViewById(R.id.etApellidos)
        etEmail = findViewById(R.id.etEmail)
        etContra = findViewById(R.id.etContra)
        etDireccion = findViewById(R.id.etDireccion)
        etCodigoPostal = findViewById(R.id.etCodigoPostal)
        btnCrearCuenta = findViewById(R.id.buttonCrearCuenta)

        btnCrearCuenta.setOnClickListener {
            if (validarCampos()) {
                guardarUsuario()
                Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                finish() // vuelve al login
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nombre = etNombre.text.toString().trim()
        val apellidos = etApellidos.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val contra = etContra.text.toString()
        val direccion = etDireccion.text.toString().trim()
        val cp = etCodigoPostal.text.toString().trim()

        // Limpiamos errores previos
        listOf(etNombre, etApellidos, etEmail, etContra, etDireccion, etCodigoPostal)
            .forEach { it.error = null }

        // Orden de arriba a abajo; mostramos el primer error y paramos
        if (nombre.isEmpty()) {
            etNombre.error = "Introduce tu nombre"
            etNombre.requestFocus()
            return false
        }
        if (apellidos.isEmpty()) {
            etApellidos.error = "Introduce tus apellidos"
            etApellidos.requestFocus()
            return false
        }
        if (email.isEmpty()) {
            etEmail.error = "Introduce tu correo electrónico"
            etEmail.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "El correo no tiene un formato válido (ej: nombre@correo.com)"
            etEmail.requestFocus()
            return false
        }
        if (contra.length < 6) {
            etContra.error = "La contraseña debe tener al menos 6 caracteres"
            etContra.requestFocus()
            return false
        }
        if (direccion.isEmpty()) {
            etDireccion.error = "Introduce tu dirección"
            etDireccion.requestFocus()
            return false
        }
        if (cp.length != 5) {
            etCodigoPostal.error = "El código postal debe tener 5 dígitos"
            etCodigoPostal.requestFocus()
            return false
        }
        // Los códigos postales españoles van de 01000 a 52999
        val cpNumero = cp.toIntOrNull()
        if (cpNumero == null || cpNumero !in 1000..52999) {
            etCodigoPostal.error = "Código postal no válido (01000 - 52999)"
            etCodigoPostal.requestFocus()
            return false
        }
        return true
    }

    private fun guardarUsuario() {
        UsuarioPrefs.get(this).edit()
            .putString(UsuarioPrefs.KEY_NOMBRE, etNombre.text.toString().trim())
            .putString(UsuarioPrefs.KEY_APELLIDOS, etApellidos.text.toString().trim())
            .putString(UsuarioPrefs.KEY_EMAIL, etEmail.text.toString().trim())
            .putString(UsuarioPrefs.KEY_CONTRA, etContra.text.toString())
            .putString(UsuarioPrefs.KEY_DIRECCION, etDireccion.text.toString().trim())
            .putString(UsuarioPrefs.KEY_CODIGO_POSTAL, etCodigoPostal.text.toString().trim())
            .apply()
    }
}