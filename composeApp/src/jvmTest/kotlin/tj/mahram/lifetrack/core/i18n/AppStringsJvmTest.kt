package tj.mahram.lifetrack.core.i18n

import java.lang.reflect.Executable
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import tj.mahram.lifetrack.domain.model.AppLanguage

/**
 * The JVM refuses to load a class with a method taking more than 255 argument
 * slots ("ClassFormatError: Too many arguments in method signature"). A data
 * class with N constructor params gets a synthetic `copy$default` with
 * N + ceil(N / 32) + 2 slots, so AppStrings must stay at 245 params or fewer —
 * at 249 the Desktop app crashed on start. Android only limits the constructor
 * call (N + 1 <= 255), which is why it kept working there.
 */
class AppStringsJvmTest {

    @Test
    fun stringsLoadOnTheJvm() {
        assertEquals("Cancel", stringsFor(AppLanguage.ENGLISH).cancelButton)
        assertEquals("Отмена", stringsFor(AppLanguage.RUSSIAN).cancelButton)
    }

    @Test
    fun everyMethodFitsTheJvmArgumentLimit() {
        val cls = AppStrings::class.java
        val executables: List<Executable> = cls.declaredMethods.toList() + cls.declaredConstructors.toList()
        // JVMS 4.3.3: at most 255 slots, counting `this` for non-static methods.
        val tooBig = executables
            .filter { e -> e.parameterTypes.sumOf { slots(it) } + (if (Modifier.isStatic(e.modifiers)) 0 else 1) > 255 }
            .map { it.name }
        assertEquals(emptyList(), tooBig)
        val ctorParams = cls.declaredConstructors.maxOf { it.parameterCount }
        assertTrue(ctorParams <= 245, "AppStrings has $ctorParams constructor params; keep it at 245 or fewer")
    }

    private fun slots(type: Class<*>): Int = if (type == java.lang.Long.TYPE || type == java.lang.Double.TYPE) 2 else 1
}
