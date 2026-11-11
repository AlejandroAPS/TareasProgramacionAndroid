package com.example.actividadevaluable_pmdm_1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HamburguesasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tapas)
        configurarBarras("HAMBURGESAS")

        val tapas = listOf(
            Articulo("Hamburguesa Básica", 9.50, R.drawable.hamburguesa_basica),
            Articulo("Hamburguesa BBQ", 12.00, R.drawable.hamburguesa_bbq),
            Articulo("Hamburguesa Ibérica", 11.50, R.drawable.hamburguesa_iberica),
            Articulo("Hambruguesa `Cabrita`", 12.50, R.drawable.hamburguesa_cabrita),
            Articulo("Hamburguesa Pollo", 10.50, R.drawable.hamburguesa_pollo)
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@HamburguesasActivity)
            adapter = ArticuloAdapter(
                tapas,
                onMas = { articulo -> mostrarFicha(articulo) { /* TODO: formulario */ } },
                onPedir = { articulo ->
                    // TODO: abrir el formulario directamente con este artículo
                    Toast.makeText(this@HamburguesasActivity, "Pedido: ${articulo.nombre}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}