package io.github.emaarco.bpmn.adapter.outbound.versioning

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.*

class VersionServiceTest {

    private val versionFileName = "bpmn-to-code.properties"
    private val underTest = VersionService(versionFileName)

    @Test
    fun `getVersion returns 0 when file does not exist`(@TempDir tempDir: Path) {
        // When the file doesn't exist, getVersion should return 0.
        val version = underTest.getVersion(tempDir.toString(), "orderFulfillment")
        assertEquals(0, version)
    }

    @Test
    fun `getVersion returns correct version from file with multiple entries`(@TempDir tempDir: Path) {
        // Prepare a properties file with multiple entries.
        val file = File(tempDir.toFile(), versionFileName)
        val properties = Properties().apply {
            setProperty("orderFulfillment", "2")
            setProperty("newsletterSubscription", "2")
        }
        file.outputStream().use { properties.store(it, null) }

        // Retrieve and assert versions for both keys.
        assertEquals(2, underTest.getVersion(tempDir.toString(), "orderFulfillment"))
        assertEquals(2, underTest.getVersion(tempDir.toString(), "newsletterSubscription"))
    }

    @Test
    fun `increaseVersion writes new version when file does not exist`(@TempDir tempDir: Path) {
        underTest.increaseVersion(tempDir.toString(), "orderFulfillment", 3)

        // After increasing, the properties file should be created and contain the correct value.
        val file = File(tempDir.toFile(), versionFileName)
        assertTrue(file.exists())

        val properties = Properties()
        file.inputStream().use { properties.load(it) }
        assertEquals("3", properties.getProperty("orderFulfillment"))
    }

    @Test
    fun `increaseVersion updates existing version in file while preserving other entries`(@TempDir tempDir: Path) {
        // Create an initial properties file with multiple entries.
        val file = File(tempDir.toFile(), versionFileName)
        val properties = Properties().apply {
            setProperty("orderFulfillment", "1")
            setProperty("newsletterSubscription", "2")
        }
        file.outputStream().use { properties.store(it, null) }

        // Update version for "orderFulfillment".
        underTest.increaseVersion(tempDir.toString(), "orderFulfillment", 4)

        // After updating, the property for "orderFulfillment" should reflect the new version.
        val updatedProperties = Properties()
        file.inputStream().use { updatedProperties.load(it) }
        assertEquals("4", updatedProperties.getProperty("orderFulfillment"))
        assertEquals("2", updatedProperties.getProperty("newsletterSubscription"))
    }
}
