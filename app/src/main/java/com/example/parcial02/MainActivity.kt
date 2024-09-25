package com.example.parcial02

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var cameraStatusText: TextView
    private lateinit var locationStatusText: TextView
    private lateinit var storageStatusText: TextView
    private lateinit var imageView: ImageView
    private lateinit var locationTextView: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enlazando vistas
        cameraStatusText = findViewById(R.id.tv_camera_status)
        locationStatusText = findViewById(R.id.tv_location_status)
        storageStatusText = findViewById(R.id.tv_storage_status)
        imageView = findViewById(R.id.imageView)
        locationTextView = findViewById(R.id.locationTextView)

        val btnCamera = findViewById<Button>(R.id.btn_camera)
        val btnLocation = findViewById<Button>(R.id.btn_location)
        val btnStorage = findViewById<Button>(R.id.btn_storage)
        val btnTakePhoto = findViewById<Button>(R.id.btn_take_photo)
        val btnGetLocation = findViewById<Button>(R.id.btn_get_location)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Solicitar permiso de Cámara
        btnCamera.setOnClickListener {
            requestPermission(Manifest.permission.CAMERA, cameraStatusText)
        }

        // Solicitar permiso de Ubicación
        btnLocation.setOnClickListener {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, locationStatusText)
        }

        // Solicitar permiso de Almacenamiento
        btnStorage.setOnClickListener {
            requestMultiplePermissions()
        }

        // Tomar foto
        btnTakePhoto.setOnClickListener {
            if (checkPermissionGranted(Manifest.permission.CAMERA)) {
                openCamera()
            } else {
                requestPermission(Manifest.permission.CAMERA, cameraStatusText)
            }
        }

        // Obtener la última ubicación conocida
        btnGetLocation.setOnClickListener {
            if (checkPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                getLastLocation()
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, locationStatusText)
            }
        }

        // Mostrar el estado inicial de los permisos
        updatePermissionStatus(Manifest.permission.CAMERA, cameraStatusText)
        updatePermissionStatus(Manifest.permission.ACCESS_FINE_LOCATION, locationStatusText)
        updatePermissionStatus(Manifest.permission.READ_EXTERNAL_STORAGE, storageStatusText)
    }

    // Función para solicitar permiso único
    private fun requestPermission(permission: String, statusText: TextView) {
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                statusText.text = "Permiso concedido"
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                statusText.text = "Permiso denegado anteriormente, solicita nuevamente"
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    // Función para solicitar múltiples permisos (lectura y escritura de almacenamiento)
    private fun requestMultiplePermissions() {
        requestMultiplePermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    // Llamada para manejar el resultado de una solicitud de permiso único
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraStatusText.text = "Permiso concedido"
        } else {
            cameraStatusText.text = "Permiso no concedido"
        }
    }

    // Llamada para manejar el resultado de una solicitud de permisos múltiples
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        val writeGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

        if (readGranted && writeGranted) {
            storageStatusText.text = "Permisos de almacenamiento concedidos"
        } else {
            storageStatusText.text = "Permisos de almacenamiento no concedidos"
        }
    }

    // Función para abrir la cámara
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            cameraActivityResultLauncher.launch(takePictureIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Manejador del resultado de la cámara
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }
    }

    // Función para obtener la última ubicación
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                locationTextView.text = "Latitud: ${location.latitude}, Longitud: ${location.longitude}"
            } else {
                locationTextView.text = "No se pudo obtener la ubicación"
            }
        }
    }

    // Verifica si el permiso ha sido concedido
    private fun checkPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    // Función para actualizar el estado del permiso en el TextView
    private fun updatePermissionStatus(permission: String, statusText: TextView) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            statusText.text = "Permiso concedido"
        } else {
            statusText.text = "Permiso no concedido"
        }
    }
}

