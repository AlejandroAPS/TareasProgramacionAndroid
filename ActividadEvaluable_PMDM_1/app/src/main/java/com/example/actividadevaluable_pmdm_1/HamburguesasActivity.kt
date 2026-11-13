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
            Articulo("Hamburguesa Básica", 9.50, R.drawable.hamburguesa_basica,
                "Carne de vacuno jugosa con lechuga, tomate, queso y pan brioche.",
                "Gluten, Lácteos, Huevos"),
            Articulo("Hamburguesa BBQ", 12.00, R.drawable.hamburguesa_bbq,
                "Hamburguesa con doble carne, bacon crujiente, queso cheddar y salsa barbacoa.",
                "Gluten, Lácteos, Huevos, Soja"),
            Articulo("Hamburguesa Ibérica", 11.50, R.drawable.hamburguesa_iberica,
                "Hamburguesa de vacuno con jamón serrano, queso manchego y aceite de oliva virgen.",
                "Gluten, Lácteos, Huevos"),
            Articulo("Hambruguesa `Cabrita`", 12.50, R.drawable.hamburguesa_cabrita,
                "Hamburguesa con queso de cabra, cebolla caramelizada y rúcula.",
                "Gluten, Lácteos, Huevos"),
            Articulo("Hamburguesa Pollo", 10.50, R.drawable.hamburguesa_pollo,
                "Pechuga de pollo empanada crujiente con lechuga, tomate y mayonesa.",
                "Gluten, Huevos, Lácteos")
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@HamburguesasActivity)
            adapter = ArticuloAdapter(
                tapas,
                onMas = { articulo -> mostrarFicha(articulo) { abrirFormulario(it) } },
                onPedir = { articulo -> abrirFormulario(articulo) }
            )
        }
    }
}