package com.example.actividadevaluable_pmdm_1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PostresActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tapas)
        configurarBarras("POSTRES")

        val tapas = listOf(
            Articulo("Brownie con helado", 3.50, R.drawable.brownie_helado
                    ),
            Articulo("Arroz con leche", 2.00, R.drawable.arroz__leche),
            Articulo("Flan", 2.00, R.drawable.flan),
            Articulo("Natillas", 2.50, R.drawable.natillas),
            Articulo("Tarta de queso", 5.00, R.drawable.tarta_queso)
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@PostresActivity)
            adapter = ArticuloAdapter(
                tapas,
                onMas = { articulo -> mostrarFicha(articulo) { /* TODO: formulario */ } },
                onPedir = { articulo ->
                    // TODO: abrir el formulario directamente con este artículo
                    Toast.makeText(this@PostresActivity, "Pedido: ${articulo.nombre}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}