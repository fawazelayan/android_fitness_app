package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Use 34 because 36 might have JVM compatibility warnings
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Scoop Tracker", appName)
  }

  @Test
  fun testViewModelInitialization() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = IntakeViewModel(application)
    assert(viewModel.creatineMax.value > 0)
  }
}
