package com.example.fortest

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date

object GGAData {
    val data : ArrayList<DoubleArray> = ArrayList()
    var referencePos : DoubleArray = doubleArrayOf(0.0,0.0,0.0,0.0)
    fun  addGga(gga: String){
        val tblh = gga2tblh(gga)
        val xyz = Utils.blhxyz(tblh[2],tblh[1],tblh[3],0.0,0.0)
        System.out.printf("vvvvvvvvv1%.5f %.5f %.5f ",xyz[0],xyz[1],xyz[2])
        data.add(doubleArrayOf(tblh[0],xyz[0],xyz[1],xyz[2]))
        if (0.toLong() == referencePos[0].toLong()){
            referencePos  = data.last()
        }
    }
    fun getLastEnu() : DoubleArray{
        val refpos = doubleArrayOf(referencePos[1],referencePos[2],referencePos[3])
        val lastpos = doubleArrayOf(data.last()[1],data.last()[2],data.last()[3])
        println("\ngetLastEnu: "+Arrays.toString(refpos))
        println("\ngetLastEnu: "+Arrays.toString(lastpos))
        val result = Utils.pos2enu(refpos, lastpos)
        for (row in result) {
            print("\ngetLastEnu: enu:")
            println(row[0])
        }
        return  doubleArrayOf(result[0][0],result[1][0],result[2][0])
    }
    fun  gga2tblh(gpggaString :String): DoubleArray{
//        val gpggaString = "\$GPGGA,003958.00,3024.9710678,N,11413.0654705,E,4,25,0.90,0.000,M,22.486,M,1.0,*75"
        val parts = gpggaString.split(',')

        // 提取时间、纬度和经度
        val time = parts[1]
        val latitude = parts[2]
        val longitude = parts[4]
        val altitude = parts[9]

        // 转换纬度和经度为十进制度
        val decimalLatitude = convertToDecimalDegrees(latitude.toDouble())
        val decimalLongitude = convertToDecimalDegrees(longitude.toDouble())

        // 转换为弧度
        val radianLatitude = Math.toRadians(decimalLatitude)
        val radianLongitude = Math.toRadians(decimalLongitude)

        return  doubleArrayOf(hhmmss2Milliseconds(time).toDouble(), radianLongitude,radianLatitude,altitude.toDouble())
}

    fun convertToDecimalDegrees(ddmm: Double): Double {
        val degrees = (ddmm / 100).toInt()
        val minutes = ddmm % 100
        return degrees + minutes / 60
    }

    fun hhmmss2Milliseconds(hhmmss: String):Long{
        try {
            // 假设日期是2024-02-23，时间是08:07:14.00
            var dateTime = "080714.00"
//
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val currentDate = dateFormat.format(Date())
            dateTime = "2024-01-01 $hhmmss"
            val sdf = SimpleDateFormat("yyyy-MM-dd HHmmss.SS")
            val date: Date = sdf.parse(dateTime)

            val millis: Long = date.getTime()
            println("Milliseconds since January 1, 1970, 00:00:00 GMT: $millis")
            return millis

        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }
}