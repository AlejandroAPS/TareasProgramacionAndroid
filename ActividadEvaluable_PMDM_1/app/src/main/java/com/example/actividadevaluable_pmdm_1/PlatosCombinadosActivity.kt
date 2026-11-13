package com.example.actividadevaluable_pmdm_1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlatosCombinadosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tapas)
        configurarBarras("COMBINADOS")

        val tapas = listOf(
            Articulo("Filete Ternera,huevos,ensalada y patatas", 12.00, R.drawable.filete_patatas_huevo_ensalada,
                "Filete de ternera a la plancha acompañado de huevos fritos, patatas y ensalada fresca.",
                "Huevos"),
            Articulo("Emperador Plancha y ensalada", 12.50, R.drawable.emperador_ensalada,
                "Filete de pez emperador a la plancha acompañado de ensalada mixta.",
                "Pescado"),
            Articulo("Pechuga Pollo,huevos y patatas", 11.00, R.drawable.pechugapollo_patatas_huevos,
                "Pechuga de pollo a la plancha con huevos fritos y patatas fritas.",
                "Huevos"),
            Articulo("Jamón con huevos y patatas", 11.50, R.drawable.jamon_huevos_patatas,
                "Plato tradicional de jamón serrano acompañado de huevos fritos y patatas.",
                "Huevos"),
            Articulo("Albóndigas con arroz", 11.00, R.drawable.albondigas_arroz,
                "Albóndigas de carne caseras en salsa tradicional acompañadas de arroz blanco.",
                "Gluten, Huevos")
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@PlatosCombinadosActivity)
            adapter = ArticuloAdapter(
                tapas,
                onMas = { articulo -> mostrarFicha(articulo) { abrirFormulario(it) } },
                onPedir = { articulo -> abrirFormulario(articulo) }
            )
        }
    }
}
