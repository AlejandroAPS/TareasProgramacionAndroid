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
                "Pincho de tortilla de patatas con cebolla, ingredientes locales y un trozo de pan para acompañar", "Huevo"),
            Articulo("Croquetas de jamón", 4.00, R.drawable.tapa_croquetas),
            Articulo("Patatas bravas", 3.00, R.drawable.tapa_patatas_bravas),
            Articulo("Calamares", 4.50, R.drawable.tapa_calamares),
            Articulo("Ensaladilla rusa", 3.50, R.drawable.tapa_ensaladilla)
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@TapasActivity)
            adapter = ArticuloAdapter(
                tapas,
                onMas = { articulo -> mostrarFicha(articulo) { /* TODO: formulario */ } },
                onPedir = { articulo ->
                    // TODO: abrir el formulario directamente con este artículo
                    Toast.makeText(this@TapasActivity, "Pedido: ${articulo.nombre}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}