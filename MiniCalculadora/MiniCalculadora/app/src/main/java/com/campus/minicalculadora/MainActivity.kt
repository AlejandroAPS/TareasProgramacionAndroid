package com.campus.minicalculadora

    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.widget.Button
    import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var btnNum1: Button
    private lateinit var btnNum2: Button
    private lateinit var btnMas: Button
    private lateinit var btnIgual: Button
    private lateinit var tvResultado: TextView

    private var num1: Int? = null
    private var num2: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnNum1 = findViewById(R.id.btnNum1)
        btnNum2 = findViewById(R.id.btnNum2)
        btnMas = findViewById(R.id.btnMas)
        btnIgual = findViewById(R.id.btnIgual)
        tvResultado = findViewById(R.id.tvResultado)

        btnNum1.setOnClickListener {
            num1 = btnNum1.text.toString().toInt()
            tvResultado.text = "$num1"
        }

        btnNum2.setOnClickListener {
            num2 = btnNum2.text.toString().toInt()
            tvResultado.text = "${num1 ?: ""} + $num2"
        }

        btnMas.setOnClickListener {
            tvResultado.text = "${num1 ?: ""} +"
        }

        btnIgual.setOnClickListener {
            val n1 = num1
            val n2 = num2
            if (n1 != null && n2 != null) {
                val resultado = n1 + n2
                tvResultado.text = "$n1 + $n2 = $resultado"
            } else {
                tvResultado.text = "Elige los dos números"
            }
        }
    }
}