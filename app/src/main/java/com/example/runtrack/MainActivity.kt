package com.example.runtrack
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runtrack.ui.theme.RunTrackTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null

    // Estados para que la pantalla de Compose se actualice solita
    private var estadoSensor by mutableStateOf("Buscando sensor...")
    private var valorX by mutableStateOf(0f)
    private var valorY by mutableStateOf(0f)
    private var valorZ by mutableStateOf(0f)

    private var ultimoTiempo: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializamos el administrador de sensores
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Verificamos si el dispositivo tiene acelerómetro
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            estadoSensor = "¡Acelerómetro activo y listo!"
        } else {
            estadoSensor = "Error: El dispositivo no tiene acelerómetro"
        }

        setContent {
            RunTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaRunTrack(
                        estado = estadoSensor,
                        x = valorX,
                        y = valorY,
                        z = valorZ,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Activamos el sensor cuando la app está en primer plano
        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Apagamos el sensor para no gastar batería de más
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val tiempoActual = System.currentTimeMillis()

            // Filtro sencillo para actualizar la pantalla cada 500ms y que no se sature
            if (tiempoActual - ultimoTiempo > 500) {
                ultimoTiempo = tiempoActual

                valorX = event.values[0]
                valorY = event.values[1]
                valorZ = event.values[2]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Método obligatorio de la interfaz, aunque no lo usemos
    }
}

// Diseño visual de la app hecho con Jetpack Compose (todo en el mismo archivo)
@Composable
fun PantallaRunTrack(estado: String, x: Float, y: Float, z: Float, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RunTrack - Compose",
            fontSize = 24.sp,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = estado,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Eje X: %.2f".format(x), fontSize = 20.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Eje Y: %.2f".format(y), fontSize = 20.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Eje Z: %.2f".format(z), fontSize = 20.sp)
            }
        }
    }
}