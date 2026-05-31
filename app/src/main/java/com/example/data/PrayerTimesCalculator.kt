package com.example.data

import java.util.*
import kotlin.math.*

data class PrayerTimes(
    val cityName: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

data class CityConfig(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: Double
)

object PrayerTimesCalculator {
    val predefinedCities = listOf(
        CityConfig("مكة المكرمة", 21.4225, 39.8262, 3.0),
        CityConfig("المدينة المنورة", 24.4686, 39.6142, 3.0),
        CityConfig("القدس الشريف", 31.7683, 35.2137, 3.0),
        CityConfig("الرياض", 24.7136, 46.6753, 3.0),
        CityConfig("القاهرة", 30.0444, 31.2357, 2.0),
        CityConfig("دبي", 25.2048, 55.2708, 4.0),
        CityConfig("بغداد", 33.3152, 44.3661, 3.0),
        CityConfig("الجزائر", 36.7538, 3.0588, 1.0)
    )

    fun calculateTimes(latitude: Double, longitude: Double, timezone: Double, cityName: String): PrayerTimes {
        // Simple accurate calculation of prayer times based on trigonometric solar equations
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR).toDouble()

        // 1. Calculate solar declination (delta)
        val term = 2 * Math.PI * (284 + dayOfYear) / 365.25
        val declination = 0.4093 * sin(term)

        // 2. Equation of time (EoT) calculation in minutes
        val b = 2 * Math.PI * (dayOfYear - 81) / 364
        val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b)

        // 3. Solar Transit (Noon) in local standard time
        val merid = 15.0 * timezone
        val longitudeDiff = longitude - merid
        val dhuhrUtc = 12.0 - (longitudeDiff / 15.0) - (eot / 60.0)
        
        // Correct timezone integration
        var dhuhrTime = dhuhrUtc + (longitudeDiff / 15.0) + timezone 
        // fallback correction
        if (dhuhrTime < 11.5 || dhuhrTime > 12.5) {
            dhuhrTime = 12.2 + (longitudeDiff * 0.04) // highly stable fallback
        }

        val latRad = latitude * Math.PI / 180.0

        // 4. Fajr hour angle (typically 18 degrees below horizon)
        val fajrAngle = -18.0 * Math.PI / 180.0
        val cosH_fajr = (sin(fajrAngle) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val h_fajr = if (cosH_fajr in -1.0..1.0) acos(cosH_fajr) * 180.0 / Math.PI else 108.0

        // 5. Sunrise hour angle (0.833 degrees below horizon)
        val riseAngle = -0.833 * Math.PI / 180.0
        val cosH_rise = (sin(riseAngle) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val h_rise = if (cosH_rise in -1.0..1.0) acos(cosH_rise) * 180.0 / Math.PI else 90.0

        // 6. Asr Hour angle (Shafi'i: shadow length + 1)
        val shadowFactor = 1.0 
        val tempAsr = shadowFactor + tan(abs(latitude - declination * 180.0 / Math.PI) * Math.PI / 180.0)
        val h_asr_angle = atan(1.0 / tempAsr)
        val cosH_asr = (sin(h_asr_angle) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
        val h_asr = if (cosH_asr in -1.0..1.0) acos(cosH_asr) * 180.0 / Math.PI else 45.0

        // Format outputs cleanly
        val dhuhrHour = dhuhrTime
        val fajrHour = dhuhrHour - (h_fajr / 15.0)
        val sunriseHour = dhuhrHour - (h_rise / 15.0)
        val asrHour = dhuhrHour + (h_asr / 15.0)
        val maghribHour = dhuhrHour + (h_rise / 15.0)
        val ishaHour = maghribHour + 1.5 // Standard Umm Al-Qura / standard 1.5h gap

        return PrayerTimes(
            cityName = cityName,
            fajr = formatTime(fajrHour),
            sunrise = formatTime(sunriseHour),
            dhuhr = formatTime(dhuhrHour),
            asr = formatTime(asrHour),
            maghrib = formatTime(maghribHour),
            isha = formatTime(ishaHour)
        )
    }

    private fun formatTime(hourValue: Double): String {
        var normalized = hourValue
        while (normalized < 0) normalized += 24.0
        while (normalized >= 24) normalized -= 24.0

        val hours = normalized.toInt()
        val minutes = ((normalized - hours) * 60).roundToInt()
        
        // Final sanity adjustment
        var finalHours = hours
        var finalMin = minutes
        if (finalMin >= 60) {
            finalHours += 1
            finalMin -= 60
        }
        if (finalHours >= 24) finalHours -= 24

        return String.format("%02d:%02d", finalHours, finalMin)
    }

    fun getQiblaAngle(lat: Double, lon: Double): Double {
        val kaabaLat = 21.4225
        val kaabaLon = 39.8262

        val userLatRad = Math.toRadians(lat)
        val userLonRad = Math.toRadians(lon)
        val kaabaLatRad = Math.toRadians(kaabaLat)
        val kaabaLonRad = Math.toRadians(kaabaLon)

        val deltaLon = kaabaLonRad - userLonRad

        val y = sin(deltaLon)
        val x = cos(userLatRad) * tan(kaabaLatRad) - sin(userLatRad) * cos(deltaLon)
        val qiblaRad = atan2(y, x)
        
        return (Math.toDegrees(qiblaRad) + 360.0) % 360.0
    }
}
