package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import tj.mahram.lifetrack.data.local.db.AppDatabase

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver = NativeSqliteDriver(
        schema = AppDatabase.Schema,
        name = "lifetrack.db"
    )
}
