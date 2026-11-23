package com.example.actividadevaluable2

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import com.example.actividadevaluable2.util.aplicarInsetsSistema
import com.example.actividadevaluable2.util.configurarEdgeToEdge
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

/**
 * Sección de IMAGEN de la Actividad Evaluable 2.
 *
 * Funcionalidades principales:
 *  - Imagen de ejemplo generada con Canvas para no depender de recursos externos.
 *  - Filtros de escala de grises y sepia recorriendo los píxeles de una copia mutable.
 *  - Conversión del Bitmap actual a JPEG y a PNG con Bitmap.compress().
 *  - Animaciones de propiedades con ObjectAnimator: rotación 360° y escala.
 */
class ImagenActivity : AppCompatActivity() {

    // Imagen original: nunca se modifica, sirve para poder restaurarla.
    private var imagenOriginal: Bitmap? = null

    // Imagen que se está mostrando actualmente (original o una copia filtrada).
    private var imagenMostrada: Bitmap? = null

    // Vistas del layout.
    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivImagen: ImageView
    private lateinit var progressImagen: ProgressBar
    private lateinit var tvEstadoImagen: TextView
    private lateinit var btnGrises: MaterialButton
    private lateinit var btnSepia: MaterialButton
    private lateinit var btnRestaurar: MaterialButton
    private lateinit var btnJpeg: MaterialButton
    private lateinit var btnPng: MaterialButton
    private lateinit var btnRotar: MaterialButton
    private lateinit var btnEscalar: MaterialButton

    // Lista de botones que se bloquean mientras se procesa la imagen.
    private lateinit var botones: List<MaterialButton>

    // Referencias a las animaciones para poder cancelarlas si se relanzan.
    private var animacionRotacion: ObjectAnimator? = null
    private var animacionEscala: AnimatorSet? = null

    /** Tipos de filtro disponibles. */
    private enum class TipoFiltro { GRISES, SEPIA }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarEdgeToEdge()
        setContentView(R.layout.activity_imagen)

        findViewById<View>(R.id.imagenRoot).aplicarInsetsSistema()
        enlazarVistas()
        configurarToolbar()
        configurarBotones()
        cargarImagenInicial()
    }

    /** Obtiene las referencias a las vistas del layout. */
    private fun enlazarVistas() {
        toolbar = findViewById(R.id.toolbarImagen)
        ivImagen = findViewById(R.id.ivImagen)
        progressImagen = findViewById(R.id.progressImagen)
        tvEstadoImagen = findViewById(R.id.tvEstadoImagen)
        btnGrises = findViewById(R.id.btnGrises)
        btnSepia = findViewById(R.id.btnSepia)
        btnRestaurar = findViewById(R.id.btnRestaurar)
        btnJpeg = findViewById(R.id.btnJpeg)
        btnPng = findViewById(R.id.btnPng)
        btnRotar = findViewById(R.id.btnRotar)
        btnEscalar = findViewById(R.id.btnEscalar)

        botones = listOf(
            btnGrises, btnSepia, btnRestaurar,
            btnJpeg, btnPng, btnRotar, btnEscalar
        )
    }

    private fun configurarToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }

    /** Asocia cada botón con la acción que realiza. */
    private fun configurarBotones() {
        // Filtros
        btnGrises.setOnClickListener { aplicarFiltro(TipoFiltro.GRISES) }
        btnSepia.setOnClickListener { aplicarFiltro(TipoFiltro.SEPIA) }
        btnRestaurar.setOnClickListener { restaurarOriginal() }

        // Conversión de formatos
        btnJpeg.setOnClickListener {
            convertirImagen(Bitmap.CompressFormat.JPEG, "jpg", calidad = 90)
        }
        btnPng.setOnClickListener {
            convertirImagen(Bitmap.CompressFormat.PNG, "png", calidad = 100)
        }

        // Animaciones de propiedades
        btnRotar.setOnClickListener { animarRotacion() }
        btnEscalar.setOnClickListener { animarEscala() }
    }

    /**
     * Crea una imagen de ejemplo con Canvas y la muestra.
     *
     * Se genera en código para que el proyecto sea autocontenido. La imagen tiene
     * colores variados, ideales para apreciar los filtros de grises y sepia.
     */
    private fun cargarImagenInicial() {
        try {
            val original = crearImagenEjemplo(ancho = 900, alto = 675)
            imagenOriginal = original
            imagenMostrada = original
            ivImagen.setImageBitmap(original)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "No hay memoria para crear la imagen de ejemplo", e)
            tvEstadoImagen.text = getString(R.string.imagen_error_filtro)
            bloquearControles(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear la imagen de ejemplo", e)
            tvEstadoImagen.text = getString(R.string.imagen_error_filtro)
            bloquearControles(true)
        }
    }

    /**
     * Dibuja un paisaje de atardecer con degradados, sol, montañas y agua.
     * Se usa una semilla fija para que las estrellas siempre queden igual.
     */
    private fun crearImagenEjemplo(ancho: Int, alto: Int): Bitmap {
        val bitmap = createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        val lienzo = Canvas(bitmap)
        val w = ancho.toFloat()
        val h = alto.toFloat()

        // 1) Cielo con degradado vertical.
        val pincelCielo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h,
                intArrayOf(
                    Color.rgb(32, 20, 64),
                    Color.rgb(120, 45, 130),
                    Color.rgb(255, 126, 95)
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        lienzo.drawRect(0f, 0f, w, h, pincelCielo)

        // 2) Estrellas en la parte alta del cielo.
        val aleatorio = Random(7)
        val pincelEstrella = Paint().apply { color = Color.argb(210, 255, 255, 255) }
        repeat(48) {
            val x = aleatorio.nextFloat() * w
            val y = aleatorio.nextFloat() * h * 0.38f
            val radio = aleatorio.nextFloat() * 2.2f + 0.7f
            lienzo.drawCircle(x, y, radio, pincelEstrella)
        }

        // 3) Sol con un halo suave.
        val solX = w * 0.70f
        val solY = h * 0.42f
        val halo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(50, 255, 224, 150)
        }
        lienzo.drawCircle(solX, solY, h * 0.18f, halo)

        val sol = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255, 226, 154) }
        lienzo.drawCircle(solX, solY, h * 0.10f, sol)

        // 4) Montañas lejanas.
        val montanaLejana = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(94, 44, 108) }
        val perfilLejano = Path().apply {
            moveTo(0f, h * 0.72f)
            lineTo(w * 0.18f, h * 0.50f)
            lineTo(w * 0.36f, h * 0.68f)
            lineTo(w * 0.55f, h * 0.44f)
            lineTo(w * 0.74f, h * 0.67f)
            lineTo(w * 0.88f, h * 0.52f)
            lineTo(w, h * 0.70f)
            lineTo(w, h * 0.80f)
            lineTo(0f, h * 0.80f)
            close()
        }
        lienzo.drawPath(perfilLejano, montanaLejana)

        // 5) Montañas cercanas, más oscuras, para dar profundidad.
        val montanaCercana = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(48, 22, 66) }
        val perfilCercano = Path().apply {
            moveTo(0f, h * 0.82f)
            lineTo(w * 0.12f, h * 0.64f)
            lineTo(w * 0.30f, h * 0.80f)
            lineTo(w * 0.45f, h * 0.60f)
            lineTo(w * 0.62f, h * 0.82f)
            lineTo(w * 0.80f, h * 0.66f)
            lineTo(w, h * 0.84f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        lienzo.drawPath(perfilCercano, montanaCercana)

        // 6) Agua con degradado y reflejo del sol.
        val pincelAgua = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, h * 0.72f, 0f, h,
                Color.rgb(58, 28, 82),
                Color.rgb(10, 8, 24),
                Shader.TileMode.CLAMP
            )
        }
        lienzo.drawRect(0f, h * 0.72f, w, h, pincelAgua)

        val pincelReflejo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(55, 255, 210, 155)
        }
        repeat(7) { indice ->
            val y = h * 0.76f + indice * h * 0.033f
            val anchoLinea = w * (0.14f - indice * 0.012f)
            lienzo.drawRoundRect(
                solX - anchoLinea / 2f,
                y,
                solX + anchoLinea / 2f,
                y + h * 0.008f,
                h * 0.004f,
                h * 0.004f,
                pincelReflejo
            )
        }

        return bitmap
    }

    // ============================================================
    //  FILTROS DE IMAGEN
    // ============================================================

    /**
     * Aplica el filtro solicitado en un hilo secundario para no congelar la interfaz.
     * Mientras procesa, se muestra un indicador y se desactivan los controles.
     */
    private fun aplicarFiltro(tipo: TipoFiltro) {
        val base = imagenOriginal
        if (base == null) {
            mostrarToast(getString(R.string.imagen_error_filtro))
            return
        }

        bloquearControles(true)
        progressImagen.isVisible = true
        tvEstadoImagen.text = getString(R.string.imagen_cargando_filtro)

        Thread {
            var resultado: Bitmap? = null
            try {
                resultado = procesarFiltro(base, tipo)
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Memoria insuficiente al aplicar el filtro", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error al aplicar el filtro", e)
            }

            runOnUiThread {
                progressImagen.isVisible = false
                bloquearControles(false)

                // Si la Activity se cerró mientras se procesaba, se descarta el resultado.
                if (isFinishing || isDestroyed) {
                    resultado?.recycle()
                    return@runOnUiThread
                }

                val filtrada = resultado
                if (filtrada != null) {
                    mostrarBitmap(filtrada)
                    val nombre = getString(
                        if (tipo == TipoFiltro.GRISES) R.string.filtro_grises
                        else R.string.filtro_sepia
                    )
                    tvEstadoImagen.text = getString(R.string.imagen_estado_filtro, nombre)
                } else {
                    tvEstadoImagen.text = getString(R.string.imagen_error_filtro)
                    mostrarToast(getString(R.string.imagen_error_filtro))
                }
            }
        }.start()
    }

    /**
     * Procesa los píxeles de una copia mutable de la imagen original.
     *
     * Se usa Bitmap.copy(config, true) para obtener una copia modificable y no
     * destruir la imagen original. Después se recorren todos los píxeles con
     * getPixels(), se transforman los canales R, G y B y se devuelven con setPixels().
     */
    private fun procesarFiltro(base: Bitmap, tipo: TipoFiltro): Bitmap {
        val ancho = base.width
        val alto = base.height

        // Copia mutable: imprescindible para poder escribir píxeles.
        val copia = base.copy(Bitmap.Config.ARGB_8888, true)
            ?: throw IllegalStateException("No se pudo crear la copia mutable del Bitmap")

        val pixeles = IntArray(ancho * alto)
        copia.getPixels(pixeles, 0, ancho, 0, 0, ancho, alto)

        for (indice in pixeles.indices) {
            val color = pixeles[indice]

            // Extracción de los canales ARGB mediante desplazamientos de bits.
            val alfa = (color ushr 24) and 0xFF
            val rojo = (color shr 16) and 0xFF
            val verde = (color shr 8) and 0xFF
            val azul = color and 0xFF

            val nuevoRojo: Int
            val nuevoVerde: Int
            val nuevoAzul: Int

            when (tipo) {
                TipoFiltro.GRISES -> {
                    // Luminancia ponderada: el ojo humano es más sensible al verde.
                    val gris = (0.299 * rojo + 0.587 * verde + 0.114 * azul)
                        .toInt()
                        .coerceIn(0, 255)
                    nuevoRojo = gris
                    nuevoVerde = gris
                    nuevoAzul = gris
                }

                TipoFiltro.SEPIA -> {
                    // Matriz clásica del filtro sepia.
                    nuevoRojo = (0.393 * rojo + 0.769 * verde + 0.189 * azul)
                        .toInt().coerceIn(0, 255)
                    nuevoVerde = (0.349 * rojo + 0.686 * verde + 0.168 * azul)
                        .toInt().coerceIn(0, 255)
                    nuevoAzul = (0.272 * rojo + 0.534 * verde + 0.131 * azul)
                        .toInt().coerceIn(0, 255)
                }
            }

            // Se recompone el píxel conservando el canal alfa original.
            pixeles[indice] = (alfa shl 24) or
                (nuevoRojo shl 16) or
                (nuevoVerde shl 8) or
                nuevoAzul
        }

        copia.setPixels(pixeles, 0, ancho, 0, 0, ancho, alto)
        return copia
    }

    /** Vuelve a mostrar la imagen original, descartando el filtro actual. */
    private fun restaurarOriginal() {
        val original = imagenOriginal
        if (original == null) {
            mostrarToast(getString(R.string.imagen_error_filtro))
            return
        }
        mostrarBitmap(original)
        tvEstadoImagen.text = getString(R.string.imagen_estado_original)
    }

    /**
     * Muestra un Bitmap en el ImageView y recicla el anterior si era una copia
     * filtrada. El original nunca se recicla para poder restaurarlo.
     */
    private fun mostrarBitmap(nuevo: Bitmap) {
        if (nuevo === imagenMostrada) {
            ivImagen.setImageBitmap(nuevo)
            return
        }

        val anterior = imagenMostrada
        imagenMostrada = nuevo
        ivImagen.setImageBitmap(nuevo)

        if (anterior != null && anterior !== imagenOriginal) {
            anterior.recycle()
        }
    }

    // ============================================================
    //  CONVERSIÓN DE FORMATOS
    // ============================================================

    /**
     * Convierte el Bitmap mostrado a JPEG o PNG usando Bitmap.compress().
     *
     * El archivo resultante se guarda en cacheDir, que no requiere permisos de
     * almacenamiento. La calidad solo tiene efecto real en JPEG; para PNG se usa 100.
     */
    private fun convertirImagen(
        formato: Bitmap.CompressFormat,
        extension: String,
        calidad: Int
    ) {
        val bitmap = imagenMostrada
        if (bitmap == null) {
            mostrarToast(getString(R.string.imagen_error_conversion))
            return
        }

        tvEstadoImagen.text = getString(R.string.imagen_estado_convirtiendo, formato.name)

        val archivo = File(cacheDir, "imagen_${System.currentTimeMillis()}.$extension")

        try {
            // FileOutputStream en modo use() garantiza que el flujo se cierre.
            val exito = FileOutputStream(archivo).use { salida ->
                bitmap.compress(formato, calidad, salida)
            }

            if (!exito) {
                throw IOException("Bitmap.compress() devolvió false")
            }

            val kilobytes = (archivo.length() / 1024).coerceAtLeast(1L).toInt()
            tvEstadoImagen.text = getString(
                R.string.imagen_estado_convertida,
                formato.name,
                kilobytes
            )

            mostrarToast(
                getString(
                    R.string.imagen_guardada,
                    formato.name,
                    kilobytes,
                    archivo.absolutePath
                )
            )
        } catch (e: IOException) {
            Log.e(TAG, "No se pudo convertir la imagen a ${formato.name}", e)
            tvEstadoImagen.text = getString(R.string.imagen_error_conversion)
            mostrarToast(getString(R.string.imagen_error_conversion))
        } catch (e: SecurityException) {
            Log.e(TAG, "Sin permiso para escribir en caché", e)
            tvEstadoImagen.text = getString(R.string.imagen_error_conversion)
            mostrarToast(getString(R.string.imagen_error_conversion))
        }
    }

    // ============================================================
    //  ANIMACIONES CON OBJECTANIMATOR
    // ============================================================

    /**
     * Animación 1: rotación completa de 360° sobre el ImageView.
     * ObjectAnimator modifica la propiedad "rotation" de la vista durante 1 segundo.
     */
    private fun animarRotacion() {
        animacionRotacion?.cancel()
        ivImagen.rotation = 0f

        animacionRotacion = ObjectAnimator.ofFloat(ivImagen, View.ROTATION, 0f, 360f).apply {
            duration = 1000L
            interpolator = DecelerateInterpolator()
            start()
        }

        tvEstadoImagen.text = getString(R.string.imagen_estado_rotacion)
    }

    /**
     * Animación 2: escala con efecto de rebote.
     * Se animan scaleX y scaleY a la vez con un AnimatorSet (1 → 1.22 → 1).
     */
    private fun animarEscala() {
        animacionEscala?.cancel()
        ivImagen.scaleX = 1f
        ivImagen.scaleY = 1f

        val escalaX = ObjectAnimator.ofFloat(ivImagen, View.SCALE_X, 1f, 1.22f, 1f)
        val escalaY = ObjectAnimator.ofFloat(ivImagen, View.SCALE_Y, 1f, 1.22f, 1f)

        animacionEscala = AnimatorSet().apply {
            playTogether(escalaX, escalaY)
            duration = 900L
            interpolator = OvershootInterpolator()
            start()
        }

        tvEstadoImagen.text = getString(R.string.imagen_estado_escala)
    }

    // ============================================================
    //  UTILIDADES
    // ============================================================

    /** Activa o desactiva los botones mientras se procesa la imagen. */
    private fun bloquearControles(bloqueado: Boolean) {
        botones.forEach { boton -> boton.isEnabled = !bloqueado }
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    /** Libera los Bitmaps y cancela animaciones al destruir la Activity. */
    override fun onDestroy() {
        super.onDestroy()

        animacionRotacion?.cancel()
        animacionEscala?.cancel()

        // Se quita primero la imagen del ImageView y después se reciclan las copias.
        ivImagen.setImageDrawable(null)

        val mostrada = imagenMostrada
        if (mostrada != null && mostrada !== imagenOriginal) {
            mostrada.recycle()
        }
        imagenOriginal?.recycle()

        imagenMostrada = null
        imagenOriginal = null
    }

    companion object {
        private const val TAG = "ImagenActivity"
    }
}
