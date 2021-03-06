package service

import models.BuildTask
import play.api.Logger

import scala.concurrent.Future

/**
 * This is the build service, which builds apps using Apache Cordova.
 * Created by daniel on 23.04.15.
 */
class PhysalisBuildService(platform: String) {

  val logger: Logger = Logger(this.getClass())
  var thread: Thread = null
  def start() = {
    logger.info(s"Start build service for ${platform}")
    thread = new Thread(() => {
      while (true) {
        newBuildTasks.foreach(processAndHandleExceptions _)
        Thread.sleep(1000)
      }
    })
    thread.start()
  }
  def stop() = {
    thread.join
  }

  private def newBuildTasks() = {
    //    def info(tasks: Seq[BuildTask]) = tasks.map(t => s"${t.platform}->${t.id},${t.project.gitUrl}")

    val tasks = BuildTask.findNew(platform)
    if (!tasks.isEmpty) logger.info(s"$platform - found BuildTasks: " + tasks.map(t => t.id.toString).mkString(","))
    tasks
  }

  def processAndHandleExceptions(buildTask: BuildTask) = {
    try {
      logger.info("processing buildtask: " + buildTask.id)
      val buildTaskInprogress = buildTask.inProgress().save()
      buildTaskInprogress.gitClone()
      buildTaskInprogress.build().save()
    } catch {
      case t: Throwable => logger.error(s"Got error in Build-Service: ${t.getMessage}", t);
    }
  }
}
