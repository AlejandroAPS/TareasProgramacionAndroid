package com.example.actividadevaluable_pmdm_1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BebidasActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tapas)
        configurarBarras("BEBIDAS")

        val tapas = listOf(
            Articulo("Cerveza Mahou", 4.00, R.drawable.cerveza_mahou),
            Articulo("Coca-cola", 3.30, R.drawable.cocacola),
            Articulo("Fanta Naranja", 3.30, R.drawable.fanta_naranja),
            Articulo("Aquarius Limon", 3.80, R.drawable.aquarius),
            Articulo("Agua", 2.30, R.drawable.agua)
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@BebidasActivity)
            adapter = ArticuloAdapter(tapas) { articulo ->
                // Por ahora solo un aviso; luego irá a la ficha / formulario
                Toast.makeText(context, "Pedido: ${articulo.nombre}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}