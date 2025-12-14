package moe.tachyon.windwhisper.console.command

import moe.tachyon.windwhisper.utils.Power
import kotlin.concurrent.thread

/**
 * 杀死服务器
 */
object Stop: Command
{
    override val description = "Stop the server."
    override suspend fun execute(sender: CommandSender, args: List<String>): Boolean
    {
        thread { Power.shutdown(0, "stop command executed.") }
        return true
    }
}