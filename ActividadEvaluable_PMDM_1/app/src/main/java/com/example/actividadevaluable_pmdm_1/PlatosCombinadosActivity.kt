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
            Articulo("Filete Ternera,huevos,ensalada y patatas", 12.00, R.drawable.filete_patatas_huevo_ensalada),
            Articulo("Emperador Plancha y ensalada", 12.50, R.drawable.emperador_ensalada),
            Articulo("Pechuga Pollo,huevos y patatas", 11.00, R.drawable.pechugapollo_patatas_huevos),
            Articulo("Jamón con huevos y patatas", 11.50, R.drawable.jamon_huevos_patatas),
            Articulo("Albóndigas con arroz", 11.00, R.drawable.albondigas_arroz)
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@PlatosCombinadosActivity)
            adapter = ArticuloAdapter(tapas) { articulo ->
                // Por ahora solo un aviso; luego irá a la ficha / formulario
                Toast.makeText(context, "Pedido: ${articulo.nombre}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}