package com.example.sprinter.classes

import com.google.android.gms.maps.model.LatLng
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*



operator fun LatLng.minus(that: LatLng): Double {

    val R = 6371e3; // metres
    val fi1 = Math.toRadians(this.latitude)
    val fi2 = Math.toRadians(that.latitude)
    val deltaFi = Math.toRadians(that.latitude-this.latitude)
    val deltaGame = Math.toRadians(that.longitude-that.longitude)

    val a = Math.sin(deltaFi/2) * Math.sin(deltaFi/2) +
            Math.cos(fi1) * Math.cos(fi2) *
            Math.sin(deltaGame/2) * Math.sin(deltaGame/2);
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    val d = R * c;

    return d

}

fun Date.stanardString(): String {
    return "${this.day}.${this.month}.${this.year}"
}
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return Math.round(this * multiplier) / multiplier
}


fun String.getUrlString(): String {
    return try {
        URLEncoder.encode(this, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        "failed " //this method would never return something with space in it so this means there was an error
    }

}