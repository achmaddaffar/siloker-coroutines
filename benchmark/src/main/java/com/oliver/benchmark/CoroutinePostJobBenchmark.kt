package com.oliver.benchmark

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.MemoryUsageMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMetricApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
class CoroutinePostJobBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun test() = benchmarkRule.measureRepeated(
        packageName = "com.oliver.siloker",
        metrics = listOf(
            StartupTimingMetric(),
            MemoryUsageMetric(MemoryUsageMetric.Mode.Last),
            MemoryUsageMetric(MemoryUsageMetric.Mode.Max),
            TraceSectionMetric("Coroutines_postJob", TraceSectionMetric.Mode.First),
        ),
        iterations = 1,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()

        login()

        device.wait(Until.hasObject(By.res(packageName, "home_job_list")), 5_000)

        val fab = device.findObject(By.res("fab"))
        fab.click()

        val titleSelector = By.res("post_job_title")
        device.wait(Until.hasObject(titleSelector), 5_000)

        val titleTextFieldSelector = By.res("title_text_field")
        device.wait(Until.hasObject(titleTextFieldSelector), 5_000)
        val titleTextField = device.findObject(titleTextFieldSelector)
        titleTextField.click()
        titleTextField.text = "Senior Android Developer (Kotlin)"

        val passwordTextFieldSelector = By.res("description_text_field")
        device.wait(Until.hasObject(passwordTextFieldSelector), 5_000)
        val passwordTextField = device.findObject(passwordTextFieldSelector)
        passwordTextField.click()
        passwordTextField.text =
            """PT Cipta Solusi Digital, sebuah perusahaan konsultan IT yang berfokus pada pengembangan aplikasi mobile, mencari Senior Android Developer yang berpengalaman untuk bergabung dengan tim inovatif kami di Malang.
            |
            |Tanggung Jawab Utama:
            |- Merancang, mengembangkan, dan memelihara aplikasi Android native menggunakan Kotlin.
            |- Bekerja sama dengan tim produk dan UI/UX untuk menciptakan pengalaman pengguna yang luar biasa.
            |- Menerapkan arsitektur modern seperti MVVM/MVI dan komponen Jetpack.
            |- Menulis kode yang bersih, teruji, dan dapat dipelihara.
            |- Melakukan code review dan membimbing developer junior.
            |
            |Kualifikasi:
            |- Pengalaman minimal 4 tahun dalam pengembangan aplikasi Android.
            |- Mahir dalam bahasa pemrograman Kotlin dan familiar dengan Java.
            |- Pemahaman mendalam tentang Android Jetpack (Compose, ViewModel, Room, Coroutines).
            |- Pengalaman dengan RESTful API dan Git.
                          """.trimMargin()

        device.pressBack()
        device.waitForIdle(3_000)

        val postBtnSelector = By.res("btn_post")
        device.wait(Until.hasObject(postBtnSelector), 5_000)
        val btn = device.findObject(postBtnSelector)
        btn.click()

        device.waitForIdle(5_000)

        device.wait(Until.hasObject(By.res("home_job_list")), 5_000)
    }
}