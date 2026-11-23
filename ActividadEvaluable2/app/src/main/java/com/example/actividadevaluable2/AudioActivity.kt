package com.example.actividadevaluable2

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.actividadevaluable2.util.aplicarInsetsSistema
import com.example.actividadevaluable2.util.configurarEdgeToEdge
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.IOException
import java.util.Locale

/**
 * Sección de AUDIO de la Actividad Evaluable 2.
 *
 * Funcionalidades principales:
 *  - Reproducción de una pista local de res/raw con MediaPlayer.create().
 *  - Reproducción de audio en streaming a partir de una URL con setDataSource(),
 *    prepareAsync() y OnPreparedListener.
 *  - Controles Play Local, Play Online, Pause y Stop con sus escuchadores.
 *  - Pausa y guardado de la posición en onPause(); liberación con release() en onDestroy().
 *  - Gestión de errores con OnErrorListener, try/catch y avisos al usuario.
 */
class AudioActivity : AppCompatActivity() {

    // Reproductor actual. Es nullable porque no existe hasta que el usuario pulsa Play.
    private var reproductor: MediaPlayer? = null

    // Posición (en milisegundos) que se guarda al pausar o al salir de la pantalla.
    private var posicionGuardada: Int = 0

    // Indica si el reproductor está pausado y puede reanudarse.
    private var enPausa: Boolean = false

    // Distingue entre la pista local y el streaming para matizar los mensajes.
    private var esStreaming: Boolean = false

    // Mientras es true, el reproductor está preparando el stream en segundo plano.
    private var cargandoStream: Boolean = false

    // Indica si la pantalla está visible para no arrancar un stream en segundo plano.
    private var pantallaVisible: Boolean = false

    // Vistas del layout.
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tilUrl: TextInputLayout
    private lateinit var etUrl: TextInputEditText
    private lateinit var btnPlayLocal: MaterialButton
    private lateinit var btnPlayOnline: MaterialButton
    private lateinit var btnPause: MaterialButton
    private lateinit var btnStop: MaterialButton
    private lateinit var seekBarAudio: SeekBar
    private lateinit var tvTiempo: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvPosicionGuardada: TextView
    private lateinit var progressStream: LinearProgressIndicator

    // Handler en el hilo principal para actualizar la barra de progreso cada 500 ms.
    private val handler = Handler(Looper.getMainLooper())

    private val actualizadorProgreso = object : Runnable {
        override fun run() {
            val mp = reproductor
            if (mp != null) {
                try {
                    val duracion = mp.duration
                    val actual = mp.currentPosition

                    if (duracion > 0) {
                        seekBarAudio.max = duracion
                        seekBarAudio.progress = actual
                        tvTiempo.text = getString(
                            R.string.tiempo_progreso,
                            formatearTiempo(actual),
                            formatearTiempo(duracion)
                        )
                    } else {
                        // En algunos streams la duración todavía se desconoce.
                        tvTiempo.text = formatearTiempo(actual)
                    }
                } catch (e: IllegalStateException) {
                    // Puede ocurrir si el reproductor se libera mientras se consulta.
                    Log.w(TAG, "No se pudo leer la posición del reproductor", e)
                }
                handler.postDelayed(this, 500L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarEdgeToEdge()
        setContentView(R.layout.activity_audio)

        findViewById<android.view.View>(R.id.audioRoot).aplicarInsetsSistema()
        enlazarVistas()
        configurarToolbar()
        configurarBotones()
        configurarSeekBar()

        // Restaura la posición guardada si la Activity se recreó (por ejemplo, al rotar).
        savedInstanceState?.let {
            posicionGuardada = it.getInt(CLAVE_POSICION, 0)
            if (posicionGuardada > 0) {
                tvTiempo.text = formatearTiempo(posicionGuardada)
                actualizarPosicionGuardada()
            }
        }
    }

    /** Obtiene las referencias a todas las vistas del layout una sola vez. */
    private fun enlazarVistas() {
        toolbar = findViewById(R.id.toolbarAudio)
        tilUrl = findViewById(R.id.tilUrl)
        etUrl = findViewById(R.id.etUrl)
        btnPlayLocal = findViewById(R.id.btnPlayLocal)
        btnPlayOnline = findViewById(R.id.btnPlayOnline)
        btnPause = findViewById(R.id.btnPause)
        btnStop = findViewById(R.id.btnStop)
        seekBarAudio = findViewById(R.id.seekBarAudio)
        tvTiempo = findViewById(R.id.tvTiempo)
        tvEstado = findViewById(R.id.tvEstado)
        tvPosicionGuardada = findViewById(R.id.tvPosicionGuardada)
        progressStream = findViewById(R.id.progressStream)

        actualizarPosicionGuardada()
    }

    /** Configura la flecha de vuelta atrás de la barra superior. */
    private fun configurarToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }

    /** Asocia cada botón obligatorio con su escuchador de eventos. */
    private fun configurarBotones() {
        btnPlayLocal.setOnClickListener { reproducirPistaLocal() }
        btnPlayOnline.setOnClickListener { reproducirStreaming() }
        btnPause.setOnClickListener { pausarOReanudar() }
        btnStop.setOnClickListener { detenerReproduccion(mostrarMensaje = true) }
    }

    /**
     * Configura el SeekBar para que el usuario pueda moverse por la pista.
     * Los cambios automáticos (fromUser = false) se ignoran para no interferir.
     */
    private fun configurarSeekBar() {
        seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(barra: SeekBar?, progreso: Int, desdeUsuario: Boolean) {
                if (desdeUsuario) {
                    tvTiempo.text = getString(
                        R.string.tiempo_progreso,
                        formatearTiempo(progreso),
                        formatearTiempo(barra?.max ?: 0)
                    )
                }
            }

            override fun onStartTrackingTouch(barra: SeekBar?) = Unit

            override fun onStopTrackingTouch(barra: SeekBar?) {
                val mp = reproductor ?: return
                val destino = barra?.progress ?: 0
                try {
                    mp.seekTo(destino)
                    posicionGuardada = destino
                    actualizarPosicionGuardada()
                } catch (e: IllegalStateException) {
                    Log.w(TAG, "No se pudo mover la reproducción", e)
                    mostrarToast(getString(R.string.audio_error_estado))
                }
            }
        })
    }

    // ============================================================
    //  REPRODUCCIÓN LOCAL (res/raw/pista.wav)
    // ============================================================

    /**
     * Reproduce la pista incluida en res/raw.
     *
     * MediaPlayer.create() deja el reproductor ya preparado, por lo que se puede
     * llamar directamente a start(). Antes se libera cualquier reproducción previa.
     */
    private fun reproducirPistaLocal() {
        ocultarTeclado()
        detenerReproduccion(mostrarMensaje = false)

        try {
            // create() devuelve un MediaPlayer ya preparado o null si no puede cargarlo.
            val pista = MediaPlayer.create(this, R.raw.pista)
            if (pista == null) {
                mostrarToast(getString(R.string.audio_error_pista_local))
                return
            }

            esStreaming = false
            cargandoStream = false
            configurarListeners(pista)
            reproductor = pista
            enPausa = false
            posicionGuardada = 0

            pista.start()

            actualizarEstado(getString(R.string.audio_estado_local))
            actualizarPosicionGuardada()
            iniciarActualizacionProgreso()
        } catch (e: IllegalStateException) {
            // Error típico si el reproductor queda en un estado inválido.
            Log.e(TAG, "Estado inválido al reproducir la pista local", e)
            manejarErrorLocal(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado al reproducir la pista local", e)
            manejarErrorLocal(e)
        }
    }

    // ============================================================
    //  STREAMING ONLINE (setDataSource + prepareAsync)
    // ============================================================

    /**
     * Reproduce un audio remoto a partir de la URL escrita por el usuario.
     *
     * Se valida la URL, se configura el MediaPlayer, se asigna el origen con
     * setDataSource() y se prepara de forma asíncrona con prepareAsync().
     * La reproducción comienza en OnPreparedListener para no bloquear la interfaz.
     */
    private fun reproducirStreaming() {
        ocultarTeclado()

        val url = etUrl.text?.toString()?.trim().orEmpty()

        // Validación básica antes de tocar el reproductor.
        val esUrlValida = URLUtil.isNetworkUrl(url) &&
            (url.startsWith("http://") || url.startsWith("https://"))
        if (!esUrlValida) {
            tilUrl.error = getString(R.string.audio_error_url)
            mostrarToast(getString(R.string.audio_error_url))
            return
        }
        tilUrl.error = null

        // Se libera cualquier reproducción anterior antes de crear la nueva.
        detenerReproduccion(mostrarMensaje = false)

        val nuevoReproductor = MediaPlayer()
        try {
            // Atributos de audio: contenido musical y uso multimedia.
            nuevoReproductor.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            // setDataSource(String) lanza IOException si la URL no es accesible.
            nuevoReproductor.setDataSource(url)
            configurarListeners(nuevoReproductor)

            esStreaming = true
            cargandoStream = true
            reproductor = nuevoReproductor

            // prepareAsync no bloquea el hilo principal; avisará en onPrepared.
            nuevoReproductor.prepareAsync()

            progressStream.isVisible = true
            actualizarEstado(getString(R.string.audio_estado_conectando))
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "URL no válida para MediaPlayer", e)
            manejarErrorConexion(nuevoReproductor, e)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permiso de red denegado", e)
            manejarErrorConexion(nuevoReproductor, e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Estado inválido al preparar el stream", e)
            manejarErrorConexion(nuevoReproductor, e)
        } catch (e: IOException) {
            Log.e(TAG, "No se pudo abrir la URL del stream", e)
            manejarErrorConexion(nuevoReproductor, e)
        }
    }

    /**
     * Instala todos los listeners del reproductor.
     *
     * Los callbacks comprueban que el reproductor que avisa sigue siendo el actual
     * (===) para ignorar eventos antiguos si el usuario ya lanzó otra reproducción.
     */
    private fun configurarListeners(mp: MediaPlayer) {
        // El stream ya está preparado: se inicia la reproducción.
        mp.setOnPreparedListener { player ->
            // Solo el streaming pasa por prepareAsync(); la pista local ya viene preparada.
            if (reproductor !== player || !esStreaming) return@setOnPreparedListener

            cargandoStream = false
            progressStream.isVisible = false
            enPausa = false
            posicionGuardada = 0

            // Si el usuario salió mientras se preparaba el stream, no se arranca solo.
            if (!pantallaVisible) {
                enPausa = true
                actualizarEstado(getString(R.string.audio_estado_preparado_pausa))
                actualizarPosicionGuardada()
                return@setOnPreparedListener
            }

            try {
                player.start()
                actualizarEstado(getString(R.string.audio_estado_online))
                actualizarPosicionGuardada()
                iniciarActualizacionProgreso()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "El stream se preparó en un estado inválido", e)
                manejarErrorLocal(e)
            }
        }

        // Muestra el porcentaje de buffer mientras se conecta el stream.
        mp.setOnBufferingUpdateListener { player, porcentaje ->
            if (reproductor === player && cargandoStream) {
                actualizarEstado(getString(R.string.audio_estado_buffering, porcentaje))
            }
        }

        // Al terminar, se liberan los recursos y se deja la interfaz lista.
        mp.setOnCompletionListener { player ->
            handler.post {
                if (reproductor === player) {
                    finalizarReproduccion()
                }
            }
        }

        // Captura errores de red, formato no soportado o URLs incorrectas.
        mp.setOnErrorListener { player, what, extra ->
            Log.e(TAG, "MediaPlayer error what=$what extra=$extra")
            handler.post {
                if (reproductor === player) {
                    manejarErrorReproduccion()
                }
            }
            // true indica que el error ya está gestionado y evita que la app se cierre.
            true
        }
    }

    // ============================================================
    //  CONTROLES: PAUSE / STOP
    // ============================================================

    /**
     * Pausa la reproducción. Si ya estaba en pausa, el mismo botón la reanuda
     * desde la posición guardada (comportamiento cómodo para el usuario).
     */
    private fun pausarOReanudar() {
        val mp = reproductor
        if (mp == null) {
            mostrarToast(getString(R.string.audio_sin_reproduccion))
            return
        }

        try {
            if (mp.isPlaying) {
                mp.pause()
                posicionGuardada = mp.currentPosition
                enPausa = true
                detenerActualizacionProgreso()
                actualizarEstado(
                    getString(R.string.audio_estado_pausa, formatearTiempo(posicionGuardada))
                )
                actualizarPosicionGuardada()
            } else if (enPausa) {
                mp.start()
                enPausa = false
                actualizarEstado(getString(R.string.audio_estado_reanudado))
                iniciarActualizacionProgreso()
            } else {
                mostrarToast(getString(R.string.audio_sin_reproduccion))
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Estado inválido al pausar o reanudar", e)
            mostrarToast(getString(R.string.audio_error_estado))
        }
    }

    /**
     * Detiene la reproducción, libera el reproductor y reinicia la interfaz.
     *
     * @param mostrarMensaje true cuando la acción la pide el usuario (botón Stop).
     */
    private fun detenerReproduccion(mostrarMensaje: Boolean) {
        val habiaReproductor = reproductor != null

        detenerActualizacionProgreso()
        liberarReproductor()

        enPausa = false
        esStreaming = false
        cargandoStream = false
        posicionGuardada = 0

        seekBarAudio.progress = 0
        tvTiempo.text = getString(R.string.tiempo_inicial)
        progressStream.isVisible = false
        actualizarPosicionGuardada()

        if (mostrarMensaje) {
            val mensaje = if (habiaReproductor) {
                getString(R.string.audio_estado_detenido)
            } else {
                getString(R.string.audio_sin_reproduccion)
            }
            actualizarEstado(mensaje)
            if (!habiaReproductor) mostrarToast(mensaje)
        }
    }

    /** Libera el MediaPlayer actual para evitar fugas de memoria. */
    private fun liberarReproductor() {
        val actual = reproductor
        reproductor = null

        actual?.let { mp ->
            try {
                // reset() devuelve el reproductor al estado Idle antes de liberarlo.
                mp.reset()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "reset() no era posible en el estado actual", e)
            }
            try {
                // release() libera los recursos nativos del reproductor.
                mp.release()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "release() no era posible en el estado actual", e)
            }
        }
    }

    // ============================================================
    //  CICLO DE VIDA
    // ============================================================

    /**
     * Si la app pasa a segundo plano, se pausa la reproducción y se guarda la
     * posición actual. Así no sigue sonando al salir y se puede continuar después.
     */
    override fun onPause() {
        pantallaVisible = false
        super.onPause()

        val mp = reproductor
        if (mp != null) {
            try {
                if (mp.isPlaying) {
                    mp.pause()
                    posicionGuardada = mp.currentPosition
                    enPausa = true
                    detenerActualizacionProgreso()
                    actualizarEstado(
                        getString(
                            R.string.audio_estado_pausa_auto,
                            formatearTiempo(posicionGuardada)
                        )
                    )
                }
            } catch (e: IllegalStateException) {
                Log.w(TAG, "No se pudo pausar la reproducción en onPause", e)
            }
        }
        actualizarPosicionGuardada()
    }

    /**
     * Al volver a la pantalla, se recoloca el reproductor en la posición guardada.
     * No se reanuda automáticamente: el usuario decide pulsando Pause/Reanudar.
     */
    override fun onResume() {
        super.onResume()
        pantallaVisible = true

        val mp = reproductor
        if (mp != null && enPausa && posicionGuardada > 0) {
            try {
                mp.seekTo(posicionGuardada)
                val duracion = mp.duration
                tvTiempo.text = if (duracion > 0) {
                    getString(
                        R.string.tiempo_progreso,
                        formatearTiempo(posicionGuardada),
                        formatearTiempo(duracion)
                    )
                } else {
                    formatearTiempo(posicionGuardada)
                }
                actualizarPosicionGuardada()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "No se pudo restaurar la posición en onResume", e)
            }
        }
    }

    /** Guarda la posición para sobrevivir a una recreación de la Activity. */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CLAVE_POSICION, posicionGuardada)
    }

    /**
     * Punto clave para evitar fugas de memoria: se cancela el Handler y se llama
     * a release() sobre el MediaPlayer. Nunca se debe salir sin liberar el reproductor.
     */
    override fun onDestroy() {
        super.onDestroy()

        detenerActualizacionProgreso()

        reproductor?.let { mp ->
            try {
                if (mp.isPlaying) posicionGuardada = mp.currentPosition
            } catch (e: IllegalStateException) {
                Log.w(TAG, "No se pudo guardar la posición en onDestroy", e)
            }
        }
        liberarReproductor()
    }

    // ============================================================
    //  GESTIÓN DE ERRORES
    // ============================================================

    /** Recupera la app de un error al reproducir la pista local. */
    private fun manejarErrorLocal(error: Exception) {
        Log.e(TAG, "Error local", error)
        detenerActualizacionProgreso()
        liberarReproductor()
        progressStream.isVisible = false
        enPausa = false
        esStreaming = false
        cargandoStream = false
        actualizarEstado(getString(R.string.audio_estado_error))
        mostrarToast(getString(R.string.audio_error_generico))
    }

    /** Recupera la app de un error al abrir o preparar un stream. */
    private fun manejarErrorConexion(mp: MediaPlayer, error: Exception) {
        Log.e(TAG, "Error de conexión con el stream", error)

        // Se libera el reproductor que no llegó a prepararse.
        try {
            mp.release()
        } catch (e: IllegalStateException) {
            Log.w(TAG, "El reproductor de stream ya estaba liberado", e)
        }

        if (reproductor === mp) reproductor = null

        detenerActualizacionProgreso()
        progressStream.isVisible = false
        enPausa = false
        esStreaming = false
        cargandoStream = false
        actualizarEstado(getString(R.string.audio_estado_error))
        mostrarToast(getString(R.string.audio_error_conexion))
    }

    /** Gestiona el callback OnErrorListener sin que la aplicación se cierre. */
    private fun manejarErrorReproduccion() {
        detenerActualizacionProgreso()
        liberarReproductor()
        progressStream.isVisible = false
        enPausa = false
        esStreaming = false
        cargandoStream = false
        actualizarEstado(getString(R.string.audio_estado_error))
        mostrarToast(getString(R.string.audio_error_reproduccion))
    }

    /** Al completarse la pista, se liberan recursos y se actualiza el estado. */
    private fun finalizarReproduccion() {
        detenerActualizacionProgreso()
        liberarReproductor()
        progressStream.isVisible = false
        enPausa = false
        esStreaming = false
        cargandoStream = false
        posicionGuardada = 0
        seekBarAudio.progress = 0
        tvTiempo.text = getString(R.string.tiempo_inicial)
        actualizarEstado(getString(R.string.audio_estado_final))
        actualizarPosicionGuardada()
    }

    // ============================================================
    //  UTILIDADES DE INTERFAZ
    // ============================================================

    private fun iniciarActualizacionProgreso() {
        handler.removeCallbacks(actualizadorProgreso)
        handler.post(actualizadorProgreso)
    }

    private fun detenerActualizacionProgreso() {
        handler.removeCallbacks(actualizadorProgreso)
    }

    private fun actualizarEstado(texto: String) {
        tvEstado.text = texto
    }

    private fun actualizarPosicionGuardada() {
        tvPosicionGuardada.text = getString(
            R.string.audio_posicion_guardada,
            formatearTiempo(posicionGuardada)
        )
    }

    /** Convierte milisegundos al formato mm:ss. */
    private fun formatearTiempo(milisegundos: Int): String {
        val seguro = milisegundos.coerceAtLeast(0)
        val totalSegundos = seguro / 1000
        val minutos = totalSegundos / 60
        val segundos = totalSegundos % 60
        return String.format(Locale.US, "%02d:%02d", minutos, segundos)
    }

    /** Oculta el teclado al iniciar una reproducción. */
    private fun ocultarTeclado() {
        val gestor = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        gestor.hideSoftInputFromWindow(etUrl.windowToken, 0)
        etUrl.clearFocus()
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "AudioActivity"
        private const val CLAVE_POSICION = "posicion_audio"
    }
}
