package com.oliver.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

fun MacrobenchmarkScope.login() {
    device.waitForIdle(5_000)

    val phoneNumberSelector = By.res("phone_number_text_field")
    device.wait(Until.hasObject(phoneNumberSelector), 5_000)
    val phoneNumberTextField = device.findObject(phoneNumberSelector)
    phoneNumberTextField.click()
    phoneNumberTextField.text = "081234567"

    val passwordSelector = By.res("password_text_field")
    device.wait(Until.hasObject(passwordSelector), 5_000)
    val passwordTextField = device.findObject(passwordSelector)
    passwordTextField.click()
    passwordTextField.text = "admin"

    device.pressBack()

    val btnLoginSelector = By.res("btn_login")
    device.wait(Until.hasObject(passwordSelector), 5_000)
    val btnLogin = device.findObject(btnLoginSelector)
    btnLogin.click()
}