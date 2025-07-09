package com.oliver.benchmark

import android.content.Intent
import android.net.Uri
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class UploadProfileBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private final val TARGET_APP_PACKAGE = "com.oliver.siloker"

    @OptIn(ExperimentalMetricApi::class)
    @Test
    fun measureUploadProfilePicture() {
        // 1. Siapkan URI gambar palsu untuk disimulasikan
        val context = InstrumentationRegistry.getInstrumentation().context
        val file = File.createTempFile("test_image", ".jpg", context.cacheDir)
        val dummyUri = Uri.fromFile(file)

        benchmarkRule.measureRepeated(
            packageName = TARGET_APP_PACKAGE,
            // 2. Tentukan metrik yang ingin diukur
            metrics = listOf(
                // Mengukur durasi "slice" yang kita definisikan dengan Trace
                TraceSectionMetric("uploadProfilePicture_Flow")
            ),
            compilationMode = CompilationMode.Full(), // Mode kompilasi untuk performa rilis
            iterations = 30, // Jumlah iterasi untuk penelitian Anda
            setupBlock = {
                // Blok ini berjalan sebelum setiap iterasi
                pressHome()
            }
        ) {
            // 3. Blok ini berisi aksi yang akan diukur

            // Buka Profile Screen secara langsung menggunakan Intent
            val intent = Intent("$TARGET_APP_PACKAGE.PROFILE_ACTION")
            // (Pastikan Anda sudah mendaftarkan action ini di AndroidManifest.xml untuk activity Anda)
            startActivityAndWait(intent)

            // Simulasikan hasil dari gallery launcher dengan mengirimkan broadcast
            // Ini adalah cara untuk memicu logika di dalam aplikasi dari luar
            val broadcastIntent = Intent("$TARGET_APP_PACKAGE.DUMMY_IMAGE_PICKED").apply {
                putExtra("DUMMY_URI", dummyUri.toString())
            }
            context.sendBroadcast(broadcastIntent)

            // Tunggu hingga proses selesai, misalnya dengan menunggu LoadingDialog hilang.
            // Anda perlu resource-id pada komponen dialog Anda.
            // device.wait(Until.gone(By.res("com.oliver.siloker:id/loading_dialog")), 10_000)
        }

        // Hapus file sementara setelah selesai
        file.delete()
    }
}