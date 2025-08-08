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
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMetricApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
class CoroutineGetJobDetailBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun test() = benchmarkRule.measureRepeated(
        packageName = "com.oliver.siloker",
        metrics = listOf(
            StartupTimingMetric(),
            MemoryUsageMetric(MemoryUsageMetric.Mode.Last),
            MemoryUsageMetric(MemoryUsageMetric.Mode.Max),
            TraceSectionMetric("Coroutine_getJobDetail", TraceSectionMetric.Mode.First),
        ),
        iterations = 1,
        startupMode = StartupMode.COLD,
        setupBlock = {
            device.executeShellCommand("pm clear $packageName")
        }
    ) {
        pressHome()
        startActivityAndWait()

        login()

        val jobListHasChild = By.hasChild(By.res("home_job_list"))
        device.wait(Until.hasObject(jobListHasChild), 5_000)

        val jobList = device.findObject(By.res("home_job_list"))
        repeat(5) {
            jobList.scroll(Direction.DOWN, 500f)
        }

        device.wait(Until.hasObject(By.text("")), 5_000)
        device.findObject(By.text("Senior Android Developer (Kotlin)")).click()

        device.waitForIdle(10_000)
        device.wait(Until.hasObject(By.res("job_image")), 10_000)
    }
}
