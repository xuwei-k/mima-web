package mima

import java.lang.ref.WeakReference

import scala.concurrent.{ExecutionContext, Future}

final class Cache[K, V >: Null, E](newValue: K => Future[Either[E, V]]) { self =>

  private[this] val map = new java.util.WeakHashMap[K, WeakReference[V]]

  def get(name: K): Future[Either[E, V]] = {
    def cached(): V = {
      val reference = self.synchronized {
        map get name
      }
      if (reference == null) null
      else reference.get
    }
    def updateCache(): Future[Either[E, V]] = {
      self.synchronized {
        map remove name
      }
      newValue(name).map {
        case x @ Right(v) =>
          self.synchronized {
            map.put(name, new WeakReference(v))
          }
          x
        case l =>
          l
      }(ExecutionContext.global)
    }

    val res = cached()
    if (res == null) updateCache()
    else Future.successful(Right(res))
  }
}
