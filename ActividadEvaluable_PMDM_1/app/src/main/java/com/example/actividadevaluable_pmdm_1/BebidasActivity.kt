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
            Articulo("Cerveza Mahou", 4.00, R.drawable.cerveza_mahou,
                "Cerveza rubia española tipo lager, fría y refrescante. 33cl.",
                "Cereales (Gluten)"),
            Articulo("Coca-cola", 3.30, R.drawable.cocacola,
                "Refresco carbonatado de cola clásico marca Coca-cola. 33cl.",
                "Sin alérgenos declarados"),
            Articulo("Fanta Naranja", 3.30, R.drawable.fanta_naranja,
                "Refresco carbonatado sabor naranja. 33cl.",
                "Sin alérgenos declarados"),
            Articulo("Aquarius Limon", 3.80, R.drawable.aquarius,
                "Bebida isotónica sabor limón. 33cl.",
                "Sin alérgenos declarados"),
            Articulo("Agua", 2.30, R.drawable.agua,
                "Botella de agua mineral natural. 50cl.",
                "Sin alérgenos declarados"),
            Articulo("Pepsi", 3.00, R.drawable.pepsi,
                "Bebida gaseosa sabor cola marca Pepsi. 50cl.",
                "Sin alérgenos declarados")
        )

        findViewById<RecyclerView>(R.id.rvArticulos).apply {
            layoutManager = LinearLayoutManager(this@BebidasActivity)
            adapter = ArticuloAdapter(
                tapas,
                onMas = { articulo -> mostrarFicha(articulo) { abrirFormulario(it) } },
                onPedir = { articulo -> abrirFormulario(articulo) }
            )
        }
    }
}
