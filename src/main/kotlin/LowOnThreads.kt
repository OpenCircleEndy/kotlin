import kotlinx.coroutines.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

fun main() {
    val spiderMonkey = Executors.newFixedThreadPool(2)

    measureTimeMillis { getDataUsingThreads(spiderMonkey) }.let { "Execution time using Threads: $it ms.".println() }
    "".println()
    measureTimeMillis { getDataUsingCoroutines(spiderMonkey) }.let { "Execution time using Coroutines: $it ms.".println() }

    spiderMonkey.shutdown()
    spiderMonkey.awaitTermination(5L, TimeUnit.SECONDS)
}

private fun String.println() = System.out.println(this)

private fun getDataUsingThreads(executorService: ExecutorService) = executorService.submit {
    "Getting data...".println()
    executorService.invokeAll(
        listOf(
            Callable { findQualityScenes("Sharknado") },
            Callable { findQualityScenes("Lake Placid vs. Anaconda") },
        )
    )
    "Done getting data.".println()
}.get()

private fun getDataUsingCoroutines(executorService: ExecutorService) = executorService.submit {
    "Getting data...".println()
    runBlocking(executorService.asCoroutineDispatcher()) {
        listOf(
            async { findQualityScenesAsync("Sharknado") },
            async { findQualityScenesAsync("Lake Placid vs. Anaconda") }
        ).awaitAll()
    }
    "Done getting data.".println()
}.get()

private fun findQualityScenes(movie: String): List<String> = "Find all quality scenes from $movie.".println()
    .also { Thread.sleep(2_000) }
    .also { "Returning all quality scenes from $movie".println() }
    .let { listOf() }

private suspend fun findQualityScenesAsync(movie: String): List<String> = "Find all quality scenes from $movie.".println()
    .also { delay(2_000) }
    .also { "Returning all quality scenes from $movie".println() }
    .let { listOf() }
