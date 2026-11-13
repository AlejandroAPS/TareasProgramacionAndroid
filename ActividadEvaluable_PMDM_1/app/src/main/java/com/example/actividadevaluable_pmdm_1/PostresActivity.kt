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
            Articulo("Brownie con helado", 3.50, R.drawable.brownie_helado,
                "Bizcocho de chocolate templado con nueces acompañado de bola de helado de vainilla.",
                "Gluten, Lácteos, Huevos, Frutos Secos"),
            Articulo("Arroz con leche", 2.00, R.drawable.arroz__leche,
                "Postre casero tradicional cremoso de arroz con leche y canela en polvo.",
                "Lácteos"),
            Articulo("Flan", 2.00, R.drawable.flan,
                "Flan de huevo casero con caramelo líquido.",
                "Huevos, Lácteos"),
            Articulo("Natillas", 2.50, R.drawable.natillas,
                "Crema dulce tradicional de vainilla con galleta María.",
                "Lácteos, Huevos, Gluten"),
            Articulo("Tarta de queso", 5.00, R.drawable.tarta_queso,
                "Porción de tarta de queso cremosa al horno con mermelada de arándanos.",
                "Lácteos, Huevos, Gluten")
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@PostresActivity)
            adapter = ArticuloAdapter(
                tapas,
                onMas = { articulo -> mostrarFicha(articulo) { abrirFormulario(it) } },
                onPedir = { articulo -> abrirFormulario(articulo) }
            )
        }
    }
}
