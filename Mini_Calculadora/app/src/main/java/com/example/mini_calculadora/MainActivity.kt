package com.example.mini_calculadora

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import java.math.BigDecimal
import java.math.MathContext


class MainActivity : AppCompatActivity() {
    private lateinit var tvOperacion: TextView
    private var acabaDeCalcular = false
    private var operacion = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvOperacion = findViewById(R.id.tvOperacion)
    }
    //Funciones con la lógica
    private fun agregarNumero(numero: String) {
        if (acabaDeCalcular) {      // nueva operación
            operacion.clear()
            acabaDeCalcular = false
        }
        operacion.append(numero)
        actualizarPantalla()
    }

    private fun agregarOperador(operador: String) {
        if (acabaDeCalcular) {
            if (operacion.toString() == "Error") operacion.clear() // tras un error no se continúa
            acabaDeCalcular = false
        }
        if (operacion.isEmpty()) return

        if (ultimoEsOperador()) {
            operacion.setLength(operacion.length - 1)
        }
        operacion.append(operador)
        actualizarPantalla()
    }
    private fun evaluar(expresion: String): BigDecimal? {
        val numeros = mutableListOf<BigDecimal>()
        val operadores = mutableListOf<Char>()
        var actual = StringBuilder()

        // 1) Separar números y operadores
        for (c in expresion) {
            if (c in "+-×÷") {
                if (actual.isEmpty() && c == '-' && numeros.isEmpty()) {
                    actual.append('-') // número negativo al principio (ej: resultado negativo)
                } else {
                    numeros.add(actual.toString().replace(',', '.').toBigDecimalOrNull() ?: return null)
                    operadores.add(c)
                    actual = StringBuilder()
                }
            } else {
                actual.append(c)
            }
        }
        numeros.add(actual.toString().replace(',', '.').toBigDecimalOrNull() ?: return null)

        // 2) Primera pasada: × y ÷ (mayor prioridad)
        val terminos = mutableListOf(numeros[0])
        val sumasRestas = mutableListOf<Char>()

        for (i in operadores.indices) {
            val siguiente = numeros[i + 1]
            when (operadores[i]) {
                '×' -> terminos[terminos.lastIndex] = terminos.last().multiply(siguiente)
                '÷' -> {
                    if (siguiente.signum() == 0) return null // división entre 0
                    terminos[terminos.lastIndex] =
                        terminos.last().divide(siguiente, MathContext.DECIMAL64)
                }
                else -> {
                    sumasRestas.add(operadores[i])
                    terminos.add(siguiente)
                }
            }
        }

        // 3) Segunda pasada: + y - (menor prioridad)
        var resultado = terminos[0]
        for (i in sumasRestas.indices) {
            resultado = if (sumasRestas[i] == '+') {
                resultado.add(terminos[i + 1])
            } else {
                resultado.subtract(terminos[i + 1])
            }
        }
        return resultado
    }

    private fun formatear(valor: BigDecimal): String {
        val texto = valor.stripTrailingZeros().toPlainString() // elimina notacion cientifica
        return texto.replace('.', ',')
    }

    private fun ultimoEsOperador(): Boolean {
        if (operacion.isEmpty()) return false
        return operacion.last() in "+-×÷"
    }
    private fun actualizarPantalla() {
        tvOperacion.text = operacion.toString()
    }

    fun btnNum0(view: View) = agregarNumero("0")
    fun btnNum1(view: View) = agregarNumero("1")
    fun btnNum2(view: View) = agregarNumero("2")
    fun btnNum3(view: View) = agregarNumero("3")
    fun btnNum4(view: View) = agregarNumero("4")
    fun btnNum5(view: View) = agregarNumero("5")
    fun btnNum6(view: View) = agregarNumero("6")
    fun btnNum7(view: View) = agregarNumero("7")
    fun btnNum8(view: View) = agregarNumero("8")
    fun btnNum9(view: View) = agregarNumero("9")

    fun btnComa(view: View) {
        if (acabaDeCalcular) {
            operacion.clear()
            acabaDeCalcular = false
        }
        // Evitar dos comas en el mismo número
        val ultimoNumero = operacion.split('+', '-', '×', '÷').last()
        if (ultimoNumero.contains(",")) return

        // Si está vacío o acaba en operador, empezar con "0,"
        if (operacion.isEmpty() || ultimoEsOperador()) {
            operacion.append("0")
        }
        operacion.append(",")
        actualizarPantalla()
    }


    fun btnSumar(view: View) = agregarOperador("+")
    fun btnRestar(view: View) = agregarOperador("-")
    fun btnMultiplicar(view: View) = agregarOperador("×")
    fun btnDividir(view: View) = agregarOperador("÷")

    // ---------- Borrar ----------

    fun btnBorrar(view: View) {
        if (acabaDeCalcular) {
            operacion.clear()
            acabaDeCalcular = false
        } else if (operacion.isNotEmpty()) {
            operacion.setLength(operacion.length - 1)
        }
        actualizarPantalla()
    }

    fun btnC(view: View) {
        operacion.clear()
        acabaDeCalcular = false
        actualizarPantalla()
    }


    fun btnIgual(view: View) {
        if (operacion.isEmpty()) return

        // Si acaba en operador (ej: "5+"), lo quitamos
        if (ultimoEsOperador()) {
            operacion.setLength(operacion.length - 1)
        }

        val resultado = evaluar(operacion.toString())

        operacion.clear()
        if (resultado == null) {
            operacion.append("Error")
        } else {
            operacion.append(formatear(resultado))
        }
        acabaDeCalcular = true
        actualizarPantalla()
    }

}