package util

object OsUtils {
    val homeDir: String
        get() {
            return if (isWindows) {
                System.getenv("USERPROFILE")
            } else {
                System.getProperty("user.home")
            }
        }
    private var OS: String? = null
    val osName: String
        get() {
            if (OS == null) {
                OS = System.getProperty("os.name")
            }
            requireNotNull(OS)
            return OS!!
        }
    val isMAC: Boolean
        get() = osName.startsWith("MAC")

    val isWindows: Boolean
        get() = osName.startsWith("Windows")

}