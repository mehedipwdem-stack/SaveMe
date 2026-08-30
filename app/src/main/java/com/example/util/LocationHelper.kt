package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val address: String,
    val mapsUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): DeviceLocation {
        return withContext(Dispatchers.IO) {
            try {
                val fusedClient: FusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)

                val location: Location? = suspendCancellableCoroutine { continuation ->
                    val cts = CancellationTokenSource()
                    fusedClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cts.token
                    ).addOnSuccessListener { loc ->
                        continuation.resume(loc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }

                    continuation.invokeOnCancellation {
                        cts.cancel()
                    }
                }

                val actualLoc = location ?: getFallbackLocation(context)

                val lat = actualLoc?.latitude ?: 23.8103
                val lng = actualLoc?.longitude ?: 90.4125
                val accuracy = actualLoc?.accuracy ?: 15.0f
                val address = resolveAddress(context, lat, lng)
                val mapsUrl = "https://maps.google.com/?q=$lat,$lng"

                DeviceLocation(
                    latitude = lat,
                    longitude = lng,
                    accuracy = accuracy,
                    address = address,
                    mapsUrl = mapsUrl
                )
            } catch (e: Exception) {
                val defaultLat = 23.8103
                val defaultLng = 90.4125
                DeviceLocation(
                    latitude = defaultLat,
                    longitude = defaultLng,
                    accuracy = 25.0f,
                    address = "Dhaka, Bangladesh",
                    mapsUrl = "https://maps.google.com/?q=$defaultLat,$defaultLng"
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getFallbackLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val gpsLoc = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) {
            null
        }

        if (gpsLoc != null) return gpsLoc

        return try {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveAddress(context: Context, latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var addressResult = ""
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        addressResult = buildAddressString(addr)
                    }
                }
                if (addressResult.isNotEmpty()) addressResult else "Lat: %.4f, Lng: %.4f".format(latitude, longitude)
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    buildAddressString(addresses[0])
                } else {
                    "Lat: %.4f, Lng: %.4f".format(latitude, longitude)
                }
            }
        } catch (e: Exception) {
            "Lat: %.4f, Lng: %.4f".format(latitude, longitude)
        }
    }

    private fun buildAddressString(address: Address): String {
        val sb = StringBuilder()
        val feature = address.featureName
        val subLocality = address.subLocality ?: address.locality
        val adminArea = address.adminArea

        if (!feature.isNullOrBlank()) sb.append(feature).append(", ")
        if (!subLocality.isNullOrBlank() && subLocality != feature) sb.append(subLocality).append(", ")
        if (!adminArea.isNullOrBlank()) sb.append(adminArea)

        val result = sb.toString().trim().removeSuffix(",")
        return if (result.isNotBlank()) result else address.getAddressLine(0) ?: "Unknown Location"
    }
}
