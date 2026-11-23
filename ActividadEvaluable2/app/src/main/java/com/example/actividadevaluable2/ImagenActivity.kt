package com.example.actividadevaluable2

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.actividadevaluable2.util.aplicarInsetsSistema
import com.example.actividadevaluable2.util.configurarEdgeToEdge
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Sección de IMAGEN de la Actividad Evaluable 2.
 *
 * Funcionalidades principales:
 *  - Imagen de ejemplo cargada desde el drawable mi_pueblo.png.
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
     * Carga la imagen mi_pueblo.png desde los recursos y la muestra.
     *
     * La imagen se decodifica con BitmapFactory reduciendo su tamaño si fuera
     * necesario, para no ocupar demasiada memoria al aplicar los filtros.
     */
    private fun cargarImagenInicial() {
        try {
            val original = decodificarImagen(R.drawable.mi_pueblo)
            imagenOriginal = original
            imagenMostrada = original
            ivImagen.setImageBitmap(original)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "No hay memoria para cargar la imagen", e)
            mostrarToast(getString(R.string.imagen_error_carga))
            bloquearControles(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar la imagen", e)
            mostrarToast(getString(R.string.imagen_error_carga))
            bloquearControles(true)
        }
    }

    /**
     * Decodifica un drawable con BitmapFactory.
     *
     * Primero se leen solo las dimensiones (inJustDecodeBounds) y después se
     * calcula inSampleSize para que el lado mayor no supere MAX_LADO_IMAGEN.
     */
    private fun decodificarImagen(recurso: Int): Bitmap {
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, recurso, medidas)

        val opciones = BitmapFactory.Options().apply {
            inSampleSize = calcularMuestra(medidas.outWidth, medidas.outHeight)
        }

        return BitmapFactory.decodeResource(resources, recurso, opciones)
            ?: throw IllegalStateException("BitmapFactory no pudo decodificar el drawable")
    }

    /** Calcula la potencia de 2 necesaria para no superar el tamaño máximo. */
    private fun calcularMuestra(ancho: Int, alto: Int): Int {
        var muestra = 1
        val ladoMayor = maxOf(ancho, alto)

        while (ladoMayor / muestra > MAX_LADO_IMAGEN) {
            muestra *= 2
        }

        return muestra
    }


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
                } else {
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
            mostrarToast(getString(R.string.imagen_error_conversion))
        } catch (e: SecurityException) {
            Log.e(TAG, "Sin permiso para escribir en caché", e)
            mostrarToast(getString(R.string.imagen_error_conversion))
        }
    }



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
    }

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

        /** Lado máximo en píxeles al decodificar el drawable, para ahorrar memoria. */
        private const val MAX_LADO_IMAGEN = 1600
    }
}
