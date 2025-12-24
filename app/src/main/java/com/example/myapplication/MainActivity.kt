package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Mostrar la información en la UI
                    mostrarAndroidIDoIMEI()
                }
            }
        }
    }
}

@Composable
fun mostrarAndroidIDoIMEI() {
    val context = LocalContext.current
    // Estado para refrescar la UI cuando se acepte el permiso
    var hasPermission by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Lanzador para pedir el permiso
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    // Pedir permiso automáticamente si es Android 9 (API 28) o inferior y no lo tenemos
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasPermission) {
            launcher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }

    val idValue = obtenerAndroidIDoIMEI(context)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "ANDROID ID (SSAID) - 16 caracteres:"
                else "IMEI (Legacy) - 15 caracteres:",
                fontWeight = FontWeight.Bold
            )
            Text(text = idValue, fontSize = 20.sp)

            // Botón de reintento si se denegó
            if (idValue == "PERMISO_DENEGADO") {
                Button(onClick = { launcher.launch(Manifest.permission.READ_PHONE_STATE) }) {
                    Text("Conceder Permiso")
                }
            }

            Text(
                text = "NOTA: Algunos dispositivos o sistemas muestran más caracteres o menos caracteres.",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 50.dp)
            )

        }
    }
}

// LA FUNCIÓN DEBE ESTAR AQUÍ (Fuera de la clase o dentro, pero visible)
fun obtenerAndroidIDoIMEI(context: Context): String {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    // Para Android 10 (API 29) hasta el 16 actual
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "ID_NO_DISPONIBLE"
    }

    // Para versiones antiguas con permisos (Android 9 API 28 o inferiores)
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.imei ?: "IMEI_NULO"
            } else {
                @Suppress("DEPRECATION")
                telephonyManager.deviceId ?: "ID_NULO"
            }
        } catch (e: SecurityException) {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }

    return "PERMISO_DENEGADO"
}