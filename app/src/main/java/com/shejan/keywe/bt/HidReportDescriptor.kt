package com.shejan.keywe.bt

/**
 * Standard USB HID Report Descriptor for a combined Bluetooth Keyboard and Mouse/Touchpad peripheral.
 * Fully compliant with USB HID Specification v1.11 & Windows hidbth.sys parser requirements.
 */
object HidReportDescriptor {

    const val MOUSE_REPORT_ID: Byte = 1
    const val KEYBOARD_REPORT_ID: Byte = 2

    // Modifier Bits for Keyboard Report
    val MODIFIER_LEFT_CTRL: Byte = (1 shl 0).toByte()
    val MODIFIER_LEFT_SHIFT: Byte = (1 shl 1).toByte()
    val MODIFIER_LEFT_ALT: Byte = (1 shl 2).toByte()
    val MODIFIER_LEFT_GUI: Byte = (1 shl 3).toByte() // Windows Key
    val MODIFIER_RIGHT_CTRL: Byte = (1 shl 4).toByte()
    val MODIFIER_RIGHT_SHIFT: Byte = (1 shl 5).toByte()
    val MODIFIER_RIGHT_ALT: Byte = (1 shl 6).toByte()
    val MODIFIER_RIGHT_GUI: Byte = (1 shl 7).toByte()

    // Mouse Button Bits
    val MOUSE_BUTTON_LEFT: Byte = (1 shl 0).toByte()
    val MOUSE_BUTTON_RIGHT: Byte = (1 shl 1).toByte()
    val MOUSE_BUTTON_MIDDLE: Byte = (1 shl 2).toByte()

    val COMBO_DESCRIPTOR = byteArrayOf(
        // --- MOUSE (Report ID 1) ---
        0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x02.toByte(), // USAGE (Mouse)
        0xA1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x85.toByte(), MOUSE_REPORT_ID, // REPORT_ID (1)
        0x09.toByte(), 0x01.toByte(), //   USAGE (Pointer)
        0xA1.toByte(), 0x00.toByte(), //   COLLECTION (Physical)

        // 3 Mouse Buttons
        0x05.toByte(), 0x09.toByte(), //     USAGE_PAGE (Button)
        0x19.toByte(), 0x01.toByte(), //     USAGE_MINIMUM (Button 1)
        0x29.toByte(), 0x03.toByte(), //     USAGE_MAXIMUM (Button 3)
        0x15.toByte(), 0x00.toByte(), //     LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //     LOGICAL_MAXIMUM (1)
        0x95.toByte(), 0x03.toByte(), //     REPORT_COUNT (3)
        0x75.toByte(), 0x01.toByte(), //     REPORT_SIZE (1)
        0x81.toByte(), 0x02.toByte(), //     INPUT (Data,Var,Abs)

        // 5-bit Padding
        0x95.toByte(), 0x01.toByte(), //     REPORT_COUNT (1)
        0x75.toByte(), 0x05.toByte(), //     REPORT_SIZE (5)
        0x81.toByte(), 0x03.toByte(), //     INPUT (Cnst,Var,Abs)

        // X, Y Relative Movement (-127 to 127)
        0x05.toByte(), 0x01.toByte(), //     USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //     USAGE (X)
        0x09.toByte(), 0x31.toByte(), //     USAGE (Y)
        0x15.toByte(), 0x81.toByte(), //     LOGICAL_MINIMUM (-127)
        0x25.toByte(), 0x7F.toByte(), //     LOGICAL_MAXIMUM (127)
        0x75.toByte(), 0x08.toByte(), //     REPORT_SIZE (8)
        0x95.toByte(), 0x02.toByte(), //     REPORT_COUNT (2)
        0x81.toByte(), 0x06.toByte(), //     INPUT (Data,Var,Rel)

        // Scroll Wheel (-127 to 127)
        0x09.toByte(), 0x38.toByte(), //     USAGE (Wheel)
        0x15.toByte(), 0x81.toByte(), //     LOGICAL_MINIMUM (-127)
        0x25.toByte(), 0x7F.toByte(), //     LOGICAL_MAXIMUM (127)
        0x75.toByte(), 0x08.toByte(), //     REPORT_SIZE (8)
        0x95.toByte(), 0x01.toByte(), //     REPORT_COUNT (1)
        0x81.toByte(), 0x06.toByte(), //     INPUT (Data,Var,Rel)

        0xC0.toByte(),                //   END_COLLECTION
        0xC0.toByte(),                // END_COLLECTION

        // --- KEYBOARD (Report ID 2) ---
        0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x06.toByte(), // USAGE (Keyboard)
        0xA1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x85.toByte(), KEYBOARD_REPORT_ID, // REPORT_ID (2)

        // 8 Modifier Keys
        0x05.toByte(), 0x07.toByte(), //   USAGE_PAGE (Keyboard/Keypad)
        0x19.toByte(), 0xE0.toByte(), //   USAGE_MINIMUM (Left Control)
        0x29.toByte(), 0xE7.toByte(), //   USAGE_MAXIMUM (Right GUI)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x95.toByte(), 0x08.toByte(), //   REPORT_COUNT (8)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)

        // Reserved Padding Byte (Constant Variable)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x81.toByte(), 0x03.toByte(), //   INPUT (Cnst,Var,Abs) -> 0x03 (Constant Variable)

        // 6 Key Array
        0x05.toByte(), 0x07.toByte(), //   USAGE_PAGE (Keyboard/Keypad)
        0x19.toByte(), 0x00.toByte(), //   USAGE_MINIMUM (0)
        0x29.toByte(), 0x65.toByte(), //   USAGE_MAXIMUM (101)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x65.toByte(), //   LOGICAL_MAXIMUM (101)
        0x95.toByte(), 0x06.toByte(), //   REPORT_COUNT (6)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x81.toByte(), 0x00.toByte(), //   INPUT (Data,Ary,Abs)

        0xC0.toByte()                 // END_COLLECTION
    )

    /**
     * Builds a 4-byte mouse report (buttons, x, y, wheel).
     */
    fun createMouseReport(buttons: Byte, dx: Byte, dy: Byte, wheel: Byte = 0): ByteArray {
        return byteArrayOf(buttons, dx, dy, wheel)
    }

    /**
     * Builds an 8-byte keyboard report (modifiers, reserved, key1, key2, key3, key4, key5, key6).
     */
    fun createKeyboardReport(modifiers: Byte, keys: ByteArray = byteArrayOf(0, 0, 0, 0, 0, 0)): ByteArray {
        val report = ByteArray(8)
        report[0] = modifiers
        report[1] = 0 // Reserved
        for (i in 0 until minOf(keys.size, 6)) {
            report[2 + i] = keys[i]
        }
        return report
    }
}
