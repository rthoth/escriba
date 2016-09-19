package test

import java.io.File
import java.net.InetSocketAddress
import java.util.Date

import io.escriba.Store
import io.escriba.server.{Config, Server}

import scala.util.Random

object TestServer {

	lazy val server: Server = {
		val s = new Server(new Config(1, 1), store)
		s.listen(new InetSocketAddress("localhost", 5000 + Random.nextInt(500)))

		s
	}

	lazy val store: Store = {
		val suffix = new Date().getTime.toHexString
		val s = new Store(new File(s"target/test/control-$suffix"), new File(s"target/test/data-$suffix").toPath, 2)

		s
	}
}

trait TestServer {
	def server = TestServer.server

	def store = TestServer.store
}
