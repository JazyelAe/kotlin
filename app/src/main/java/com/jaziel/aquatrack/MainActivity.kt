package com.jaziel.aquatrack

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Meta diaria de hidratación en mililitros. */
const val META_ML = 2000

/** Una toma de agua: hora (HH:mm) y cantidad en ml. */
data class Registro(val hora: String, val ml: Int)

/** Guarda y carga los registros del día usando SharedPreferences (almacenamiento local). */
class Almacen(context: Context) {
    private val prefs = context.getSharedPreferences("aquatrack", Context.MODE_PRIVATE)

    private fun hoy(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun cargar(): List<Registro> {
        // Si ya cambió el día, se empieza de cero.
        if (prefs.getString("fecha", "") != hoy()) {
            guardar(emptyList())
            return emptyList()
        }
        val texto = prefs.getString("registros", "") ?: ""
        if (texto.isEmpty()) return emptyList()
        return texto.split(";").mapNotNull { item ->
            val partes = item.split("|")
            val ml = partes.getOrNull(1)?.toIntOrNull()
            if (partes.size == 2 && ml != null) Registro(partes[0], ml) else null
        }
    }

    fun guardar(lista: List<Registro>) {
        prefs.edit()
            .putString("fecha", hoy())
            .putString("registros", lista.joinToString(";") { "${it.hora}|${it.ml}" })
            .apply()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF0B7DDA),
                    secondary = Color(0xFF0F9D8A),
                    background = Color(0xFFF3F9FE)
                )
            ) {
                AquaTrackApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AquaTrackApp() {
    val context = LocalContext.current
    val almacen = remember { Almacen(context) }
    var registros by remember { mutableStateOf(almacen.cargar()) }

    val total = registros.sumOf { it.ml }
    val progreso = (total.toFloat() / META_ML).coerceAtMost(1f)
    val porcentaje = (progreso * 100).toInt()

    fun agregar(ml: Int) {
        val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        registros = registros + Registro(hora, ml)
        almacen.guardar(registros)
    }

    fun reiniciar() {
        registros = emptyList()
        almacen.guardar(registros)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AquaTrack", fontWeight = FontWeight.Bold)
                        Text("Contador de hidratación", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicador circular de avance
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                val colorFondo = Color(0xFFD6E8F7)
                val colorAvance = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val grosor = 22.dp.toPx()
                    drawArc(
                        color = colorFondo,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = grosor)
                    )
                    drawArc(
                        color = colorAvance,
                        startAngle = -90f,
                        sweepAngle = 360f * progreso,
                        useCenter = false,
                        style = Stroke(width = grosor, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$total", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text("de $META_ML ml", fontSize = 13.sp)
                    Text(
                        "$porcentaje%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = when {
                    total >= META_ML -> "🎉 ¡Meta diaria alcanzada!"
                    total > 0 -> "Vas muy bien, ¡sigue así!"
                    else -> ""
                },
                fontSize = 14.sp
            )
            Spacer(Modifier.height(10.dp))

            // Botones para registrar agua
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { agregar(250) }, modifier = Modifier.weight(1f)) {
                    Text("+ 250 ml")
                }
                Button(
                    onClick = { agregar(500) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("+ 500 ml")
                }
            }

            // Registro del día
            Spacer(Modifier.height(16.dp))
            Text(
                "Registro de hoy",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            if (registros.isEmpty()) {
                Text(
                    "Aún no registras agua hoy.",
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(registros.reversed()) { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("💧 ${r.hora}")
                        Text("+${r.ml} ml", fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                }
            }

            OutlinedButton(onClick = { reiniciar() }, modifier = Modifier.fillMaxWidth()) {
                Text("Reiniciar día", color = Color(0xFFC0392B))
            }
        }
    }
}
