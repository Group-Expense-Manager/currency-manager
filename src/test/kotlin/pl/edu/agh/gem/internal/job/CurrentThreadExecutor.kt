package pl.edu.agh.gem.internal.job

import java.util.concurrent.Executor

class CurrentThreadExecutor : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }
}
