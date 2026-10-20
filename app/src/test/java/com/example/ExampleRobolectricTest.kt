package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ConnectionPayload
import com.example.model.formatFileSize
import com.example.qrcode.QRCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TajSend", appName)
    }

    @Test
    fun `test connection payload serialization and parsing`() {
        val payload = ConnectionPayload(
            ip = "192.168.1.15",
            port = 8888,
            deviceName = "TajSend Device",
            pinToken = "948271"
        )
        val qrString = payload.toQrString()
        assertTrue(qrString.startsWith("tajsend://"))

        val parsed = ConnectionPayload.fromQrString(qrString)
        assertNotNull(parsed)
        assertEquals("192.168.1.15", parsed?.ip)
        assertEquals(8888, parsed?.port)
        assertEquals("948271", parsed?.pinToken)
    }

    @Test
    fun `test formatFileSize`() {
        assertEquals("0 Б", formatFileSize(0L))
        assertEquals("1.0 КБ", formatFileSize(1024L))
        assertEquals("5.0 МБ", formatFileSize(5L * 1024L * 1024L))
    }

    @Test
    fun `test QRCodeGenerator generates bitmap`() {
        val bitmap = QRCodeGenerator.generateQRCode("tajsend://test", 100, 100)
        assertNotNull(bitmap)
        assertEquals(100, bitmap?.width)
        assertEquals(100, bitmap?.height)
    }
}
