package com.example.actividadevaluable_pmdm_1

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ArticuloAdapter(
    private val articulos: List<Articulo>,
    private val onMas: (Articulo) -> Unit,
    private val onPedir: (Articulo) -> Unit
) : RecyclerView.Adapter<ArticuloAdapter.VH>() {

    class VH(vista: android.view.View) : RecyclerView.ViewHolder(vista) {
        val imagen: ImageView = vista.findViewById(R.id.ivArticulo)
        val nombre: TextView = vista.findViewById(R.id.tvNombre)
        val precio: TextView = vista.findViewById(R.id.tvPrecio)
        val btnMas: Button = vista.findViewById(R.id.btnMas)
        val btnPedir: Button = vista.findViewById(R.id.btnPedir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_tarjeta, parent, false))

    override fun getItemCount() = articulos.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = articulos[position]
        holder.imagen.setImageResource(a.imagen)
        holder.nombre.text = a.nombre
        holder.precio.text = String.format(Locale("es", "ES"), "%.2f €", a.precio)
        holder.btnMas.setOnClickListener { onMas(a) }
        holder.btnPedir.setOnClickListener { onPedir(a) }
        holder.itemView.setOnClickListener { onMas(a) } // pulsar la tarjeta también abre la ficha
    }
}