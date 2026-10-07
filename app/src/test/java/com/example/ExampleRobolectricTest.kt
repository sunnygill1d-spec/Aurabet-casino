package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ProvablyFairEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AuraBet", appName)
    }

    @Test
    fun `provably fair engine produces deterministic SHA256 and outcomes`() {
        val testSeed = "test_server_seed_12345"
        val clientSeed = "client_abc"
        val nonce = 1L

        val hash1 = ProvablyFairEngine.sha256(testSeed)
        val hash2 = ProvablyFairEngine.sha256(testSeed)
        assertEquals(hash1, hash2)

        val crash1 = ProvablyFairEngine.calculateCrashOutcome(testSeed, clientSeed, nonce)
        val crash2 = ProvablyFairEngine.calculateCrashOutcome(testSeed, clientSeed, nonce)
        assertEquals(crash1, crash2, 0.001)
        assertTrue(crash1 >= 1.0)

        val diceOutcome = ProvablyFairEngine.calculateDiceOutcome(testSeed, clientSeed, nonce)
        assertTrue(diceOutcome in 0.0..99.99)
    }
}
