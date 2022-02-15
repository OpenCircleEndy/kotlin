import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun main() {
    singletonRoutine()
    instancePerClassRoutine()
}

private fun singletonRoutine() {
    println("\n--- singletonRoutine ---\n")
    val asyncThatMap = AsyncMap()

    val threads = listOf(
        Thread(Converter(asyncThatMap)),
        Thread(Converter(asyncThatMap)),
        Thread(Converter(asyncThatMap)),
    )

    threads.onEach { it.start() }
    threads.onEach { it.join() }

    asyncThatMap.shutdown()
}

private fun instancePerClassRoutine() {
    println("\n--- instancePerClassRoutine ---\n")

    val converters = listOf(
        Converter(AsyncMap()),
        Converter(AsyncMap()),
        Converter(AsyncMap()),
    )

    val threads = converters.map { Thread(it) }

    threads.onEach { it.start() }
    threads.onEach { it.join() }
    converters.onEach { it.shutdown() }
}

class AsyncMap {
    private val executor = Executors.newFixedThreadPool(4)
    private val dispatcher = executor.asCoroutineDispatcher()

    fun go(id: Long) = runBlocking {
        println("Map items for $id.....")
        (1..10).toList().asyncMap(dispatcher) { convert(id, it) }
        println("Done with $id!")
    }

    fun shutdown() = executor.shutdown()

    private fun convert(id: Long, i: Int) {
        println("Converting $i for $id.")
        Thread.sleep(5000)
    }
}

class Converter(private val asyncMap: AsyncMap): Runnable {
    override fun run() {
        asyncMap.go(Thread.currentThread().id)
    }

    fun shutdown() = asyncMap.shutdown()
}

suspend inline fun <T, R> Iterable<T>.asyncMap(context: CoroutineContext = EmptyCoroutineContext, crossinline transform: suspend (T) -> R): List<R> =
    coroutineScope {
        map { async(context) { transform(it) } }.awaitAll()
    }
