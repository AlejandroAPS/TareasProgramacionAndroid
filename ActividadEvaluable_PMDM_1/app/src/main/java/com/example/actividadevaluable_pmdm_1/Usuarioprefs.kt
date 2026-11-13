package com.example.actividadevaluable_pmdm_1

import android.content.Context
import android.content.SharedPreferences

object UsuarioPrefs {
    private const val NOMBRE_ARCHIVO = "usuario_prefs"

    const val KEY_NOMBRE = "nombre"
    const val KEY_APELLIDOS = "apellidos"
    const val KEY_EMAIL = "email"
    const val KEY_CONTRA = "contra"
    const val KEY_DIRECCION = "direccion"
    const val KEY_CODIGO_POSTAL = "codigo_postal"

    fun get(context: Context): SharedPreferences = context.getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)

    fun hayUsuarioRegistrado(context: Context): Boolean = get(context).contains(KEY_EMAIL)
}