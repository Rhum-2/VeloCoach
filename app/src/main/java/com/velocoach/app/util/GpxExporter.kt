package com.velocoach.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.velocoach.app.data.RideEntity
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GpxExporter {

    fun share(context: Context, ride: RideEntity) {
        val uri = export(context, ride)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/gpx+xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exporter la sortie"))
    }

    private fun export(context: Context, ride: RideEntity): android.net.Uri {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "velocoach-" +
            SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date(ride.startedAt)) + ".gpx"
        val file = File(dir, fileName)
        file.writeText(buildGpx(ride))
        return FileProvider.getUriForFile(context, "com.velocoach.app.fileprovider", file)
    }

    private fun buildGpx(ride: RideEntity): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")
        val points = JSONArray(ride.pointsJson)

        val sb = StringBuilder()
        sb.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx version=\"1.1\" creator=\"VeloCoach\" " +
                "xmlns=\"http://www.topografix.com/GPX/1/1\">\n"
        )
        sb.append("  <trk>\n")
        sb.append("    <name>Sortie VéloCoach</name>\n")
        sb.append("    <trkseg>\n")
        for (i in 0 until points.length()) {
            val point = points.getJSONObject(i)
            sb.append("      <trkpt lat=\"${point.getDouble("lat")}\" lon=\"${point.getDouble("lon")}\">\n")
            sb.append("        <ele>${point.getDouble("ele")}</ele>\n")
            sb.append("        <time>${isoFormat.format(Date(point.getLong("t")))}</time>\n")
            sb.append("      </trkpt>\n")
        }
        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")
        sb.append("</gpx>\n")
        return sb.toString()
    }
}
