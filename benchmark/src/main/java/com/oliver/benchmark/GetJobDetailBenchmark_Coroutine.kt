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
class GetJobDetailBenchmark_Coroutine {
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
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()

        if (device.hasObject(By.text("Login"))) {
            login()
        }

        val jobListHasChild = By.hasChild(By.res("home_job_list"))
        device.wait(Until.hasObject(jobListHasChild), 5_000)

        val jobList = device.findObject(By.res("home_job_list"))
        jobList.scroll(Direction.DOWN, 200f)

        device.findObject(By.text("Data Engineer at SiLoker")).click()
        device.wait(Until.hasObject(By.text("Apply")), 5_000)
    }
}
