package proj.racquell.mobiledev.notificationrepublisher

import java.util.zip.CRC32

object Ids {
    fun stableIntId(key: String): Int {
        val crc = CRC32()
        crc.update(key.toByteArray(Charsets.UTF_8))
        return (crc.value and 0x7FFFFFFF).toInt()
    }
}