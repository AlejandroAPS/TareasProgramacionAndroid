package com.example.actividadevaluable_pmdm_1

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.Calendar
import java.util.Locale

class FormularioActivity : AppCompatActivity() {

    private lateinit var articulo: Articulo

    private lateinit var grupoEntrega: MaterialButtonToggleGroup
    private lateinit var grupoPago: MaterialButtonToggleGroup
    private lateinit var seccionOtraDireccion: View
    private lateinit var seccionTarjeta: View
    private lateinit var tvDireccionGuardada: TextView

    private lateinit var etCantidad: EditText
    private lateinit var etNombreDest: EditText
    private lateinit var etDireccionDest: EditText
    private lateinit var etCpDest: EditText
    private lateinit var etTitular: EditText
    private lateinit var etNumTarjeta: EditText
    private lateinit var etCaducidad: EditText
    private lateinit var etCvc: EditText
    private lateinit var cbCondiciones: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario)
        configurarBarraAtras("PEDIDO")

        @Suppress("DEPRECATION")
        articulo = (intent.getSerializableExtra("articulo") as? Articulo) ?: run {
            finish()
            return
        }

        enlazarVistas()
        pintarTarjeta()
        pintarDireccionGuardada()
        configurarSecciones()
        configurarFormatoCaducidad()

        findViewById<Button>(R.id.btnConfirmar).setOnClickListener {
            if (validar()) {
                // TODO: abrir la pantalla de confirmación con el artículo,
                // la cantidad, la entrega, el pago y las notas
                Toast.makeText(this, "Datos correctos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enlazarVistas() {
        grupoEntrega = findViewById(R.id.grupoEntrega)
        grupoPago = findViewById(R.id.grupoPago)
        seccionOtraDireccion = findViewById(R.id.seccionOtraDireccion)
        seccionTarjeta = findViewById(R.id.seccionTarjeta)
        tvDireccionGuardada = findViewById(R.id.tvDireccionGuardada)
        etCantidad = findViewById(R.id.etCantidad)
        etNombreDest = findViewById(R.id.etNombreDest)
        etDireccionDest = findViewById(R.id.etDireccionDest)
        etCpDest = findViewById(R.id.etCpDest)
        etTitular = findViewById(R.id.etTitular)
        etNumTarjeta = findViewById(R.id.etNumTarjeta)
        etCaducidad = findViewById(R.id.etCaducidad)
        etCvc = findViewById(R.id.etCvc)
        cbCondiciones = findViewById(R.id.cbCondiciones)
    }

    private fun pintarTarjeta() {
        findViewById<ImageView>(R.id.ivArticulo).setImageResource(articulo.imagen)
        findViewById<TextView>(R.id.tvNombre).text = articulo.nombre
        findViewById<TextView>(R.id.tvPrecio).text =
            String.format(Locale("es", "ES"), "%.2f €", articulo.precio)
        findViewById<Button>(R.id.btnPedir).visibility = View.GONE // ya estamos pidiendo
        findViewById<Button>(R.id.btnMas).setOnClickListener {
            mostrarFicha(articulo, mostrarPedido = false)
        }
    }

    private fun pintarDireccionGuardada() {
        val prefs = UsuarioPrefs.get(this)
        val direccion = prefs.getString(UsuarioPrefs.KEY_DIRECCION, "") ?: ""
        val cp = prefs.getString(UsuarioPrefs.KEY_CODIGO_POSTAL, "") ?: ""
        tvDireccionGuardada.text = "Se enviará a:\n$direccion, $cp"
    }

    private fun esOtraDireccion() = grupoEntrega.checkedButtonId == R.id.btnOtraDireccion
    private fun pagaConTarjeta() = grupoPago.checkedButtonId == R.id.btnTarjeta

    private fun configurarSecciones() {
        grupoEntrega.addOnButtonCheckedListener { _, _, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val otra = esOtraDireccion()
            tvDireccionGuardada.visibility = if (otra) View.GONE else View.VISIBLE
            seccionOtraDireccion.visibility = if (otra) View.VISIBLE else View.GONE
        }
        grupoPago.addOnButtonCheckedListener { _, _, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            seccionTarjeta.visibility = if (pagaConTarjeta()) View.VISIBLE else View.GONE
        }
    }

    // Escribe "0828" y lo convierte en "08/28"
    private fun configurarFormatoCaducidad() {
        etCaducidad.addTextChangedListener(object : TextWatcher {
            private var editando = false
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (editando || s == null) return
                editando = true
                val digitos = s.toString().filter { it.isDigit() }.take(4)
                val nuevo = if (digitos.length > 2)
                    digitos.substring(0, 2) + "/" + digitos.substring(2) else digitos
                s.replace(0, s.length, nuevo)
                editando = false
            }
        })
    }

    private fun EditText.fallo(mensaje: String): Boolean {
        error = mensaje
        requestFocus()
        return false
    }

    private fun validar(): Boolean {
        listOf(
            etCantidad, etNombreDest, etDireccionDest, etCpDest,
            etTitular, etNumTarjeta, etCaducidad, etCvc
        ).forEach { it.error = null }

        val cantidad = etCantidad.text.toString().trim().toIntOrNull()
        if (cantidad == null || cantidad !in 1..20)
            return etCantidad.fallo("Introduce una cantidad entre 1 y 20")

        if (esOtraDireccion()) {
            if (etNombreDest.text.isBlank())
                return etNombreDest.fallo("Indica a nombre de quién va el pedido")
            if (etDireccionDest.text.isBlank())
                return etDireccionDest.fallo("Introduce la dirección de entrega")
            val cp = etCpDest.text.toString().trim()
            val cpNumero = cp.toIntOrNull()
            if (cp.length != 5 || cpNumero == null)
                return etCpDest.fallo("El código postal debe tener 5 dígitos")
            if (cpNumero !in 1000..52999)
                return etCpDest.fallo("Código postal no válido (01000 - 52999)")
        }

        if (pagaConTarjeta()) {
            if (etTitular.text.isBlank())
                return etTitular.fallo("Introduce el nombre del titular")
            if (!etNumTarjeta.text.toString().matches(Regex("\\d{16}")))
                return etNumTarjeta.fallo("El número de tarjeta debe tener 16 dígitos")

            val m = Regex("(\\d{2})/(\\d{2})").matchEntire(etCaducidad.text.toString())
                ?: return etCaducidad.fallo("Formato MM/AA (ej: 08/28)")
            val mes = m.groupValues[1].toInt()
            val anio = 2000 + m.groupValues[2].toInt()
            if (mes !in 1..12)
                return etCaducidad.fallo("El mes debe estar entre 01 y 12")
            val hoy = Calendar.getInstance()
            val anioActual = hoy.get(Calendar.YEAR)
            val mesActual = hoy.get(Calendar.MONTH) + 1
            if (anio < anioActual || (anio == anioActual && mes < mesActual))
                return etCaducidad.fallo("La tarjeta está caducada")

            if (!etCvc.text.toString().matches(Regex("\\d{3}")))
                return etCvc.fallo("El CVC debe tener 3 dígitos")
        }

        if (!cbCondiciones.isChecked) {
            Toast.makeText(this, "Debes aceptar las condiciones del pedido", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}