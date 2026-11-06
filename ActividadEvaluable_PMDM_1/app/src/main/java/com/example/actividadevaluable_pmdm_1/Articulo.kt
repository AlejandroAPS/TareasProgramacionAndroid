package com.example.actividadevaluable_pmdm_1
import androidx.annotation.DrawableRes

data class Articulo(
    val nombre: String,
    val precio: Double,
    @DrawableRes val imagen: Int
)