package moe.tachyon.windwhisper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.tachyon.windwhisper.config.ConfigLoader
import moe.tachyon.windwhisper.console.Console
import moe.tachyon.windwhisper.console.command.CommandSet
import moe.tachyon.windwhisper.forum.login
import moe.tachyon.windwhisper.utils.Power
import java.io.File
import kotlin.properties.Delegates

lateinit var version: String
    private set
lateinit var workDir: File
    private set
lateinit var dataDir: File
    private set
lateinit var mainConfig: MainConfig
var debug by Delegates.notNull<Boolean>()
    private set

/**
 * 解析命令行, 返回的是处理后的命令行, 和从命令行中读取的配置文件(默认是config.yaml, 可通过-config=xxxx.yaml更改)
 */
private fun parseCommandLineArgs(args: Array<String>): Pair<Array<String>, File>
{
    val argsMap = args.mapNotNull()
    {
        when (val idx = it.indexOf("="))
        {
            -1 -> null
            else -> Pair(it.take(idx), it.drop(idx + 1))
        }
    }.toMap()

    // 从命令行中加载信息

    // 工作目录
    workDir = File(argsMap["-workDir"] ?: ".")
    workDir.mkdirs()

    // 配置文件目录
    dataDir = argsMap["-dataDir"]?.let { File(it) } ?: File(workDir, "data")
    dataDir.mkdirs()

    // 是否开启debug模式
    debug = argsMap["-debug"].toBoolean()
    System.setProperty("io.ktor.development", "$debug")

    // 去除命令行中的-config参数, 因为ktor会解析此参数进而不加载打包的application.yaml
    // 其余参数还原为字符串数组
    val resArgs = argsMap.entries
        .filterNot { it.key == "-config" || it.key == "-workDir" || it.key == "-debug" || it.key == "-dataDir" }
        .map { (k, v) -> "$k=$v" }
        .toTypedArray()
    // 命令行中输入的自定义配置文件
    val configFile = argsMap["-config"]?.let { File(it) } ?: File(workDir, "config.json")

    return resArgs to configFile
}

suspend fun main(args: Array<String>) = withContext(Dispatchers.Default)
{
    println("starting WindWhisper...")
    val (_, configFile) = runCatching { parseCommandLineArgs(args) }.getOrElse { return@withContext }
    ConfigLoader.init()
    Power.init()
    Console.startConsoleCommandHandler()
    println("initialized Power system.")
    if (!configFile.exists())
    {
        val default = MainConfig(
            url = "https://xxx.com",
            username = "your_username",
            password = "your_password",
            retry = 3,
            defaultHeaders = mapOf()
        )
        configFile.writeText(showJson.encodeToString(MainConfig.serializer(), default))
        println("配置文件不存在, 已生成默认配置文件于 ${configFile.absolutePath}, 请修改后重新运行")
        return@withContext
    }
    val promptFile = File(workDir, "prompt.txt")
    if (!promptFile.exists())
    {
        Loader.getResource("/prompt.default.md")?.use { input ->
            promptFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
    val prompt = promptFile.readText()
    mainConfig = dataJson.decodeFromString(configFile.readText())
    println("Config loaded from ${configFile.absolutePath}")
    workMain(login(), prompt)
}
