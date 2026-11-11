package com.example.actividadevaluable_pmdm_1
import androidx.annotation.DrawableRes
import java.io.Serializable

data class Articulo(
    val nombre: String,
    val precio: Double,
    @DrawableRes val imagen: Int,
    val descripcion: String = "Sin descripción disponible.",
    val alergenos: String = "Sin alérgenos declarados"
) : Serializable