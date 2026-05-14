package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import tj.mahram.lifetrack.data.local.db.AppDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        val dbPath = System.getProperty("user.home") + File.separator + ".lifetrack" + File.separator + "lifetrack.db"
        File(dbPath).parentFile?.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
        val currentVersion = driver.executeQuery(
            identifier = null,
            sql = "PRAGMA user_version;",
            mapper = { cursor -> QueryResult.Value(cursor.getLong(0) ?: 0L) },
            parameters = 0
        ).value
        val tableCount = driver.executeQuery(
            identifier = null,
            sql = "SELECT COUNT(*) FROM sqlite_master WHERE type='table';",
            mapper = { cursor -> QueryResult.Value(cursor.getLong(0) ?: 0L) },
            parameters = 0
        ).value
        val newVersion = AppDatabase.Schema.version
        when {
            tableCount == 0L -> {
                AppDatabase.Schema.create(driver)
                driver.execute(null, "PRAGMA user_version = $newVersion;", 0)
            }
            currentVersion == 0L -> {
                // existing DB created before version tracking was added
                driver.execute(null, "PRAGMA user_version = $newVersion;", 0)
            }
            currentVersion < newVersion -> {
                AppDatabase.Schema.migrate(driver, currentVersion, newVersion)
                driver.execute(null, "PRAGMA user_version = $newVersion;", 0)
            }
        }
        return driver
    }
}
