package com.example.actividadevaluable_pmdm_1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TapasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tapas)
        configurarBarras("TAPAS")

        val tapas = listOf(
            Articulo("Tortilla de patatas", 3.50, R.drawable.tapa_tortilla,
                "Clásica tortilla española de patatas con cebolla, jugosa y recién hecha.",
                "Huevos"),
            Articulo("Croquetas de jamón", 4.00, R.drawable.tapa_croquetas,
                "Croquetas caseras cremosas rellenas de trocitos de jamón serrano.",
                "Gluten, Lácteos, Huevos"),
            Articulo("Patatas bravas", 3.00, R.drawable.tapa_patatas_bravas,
                "Patatas bravas típicas bañadas en salsa brava picante y/o salsa alioli(solicite al camarero de turno).",
                "Huevos, Gluten"),
            Articulo("Calamares", 4.50, R.drawable.tapa_calamares,
                "Calamares frescos rebozados y fritos, servidos con gajo de limón.",
                "Pescado, Gluten, Moluscos"),
            Articulo("Ensaladilla rusa", 3.50, R.drawable.tapa_ensaladilla,
                "Ensalada fría tradicional de patata, zanahoria, guisantes, atún y mayonesa.",
                "Huevos, Pescado")
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@TapasActivity)
            adapter = ArticuloAdapter(
                tapas,
                onMas = { articulo -> mostrarFicha(articulo) { abrirFormulario(it) } },
                onPedir = { articulo -> abrirFormulario(articulo) }
            )
        }
    }
}
