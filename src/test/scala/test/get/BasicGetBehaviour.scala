package test.get

import io.escriba.DataEntry
import org.junit.runner.RunWith
import org.specs2.Specification
import org.specs2.runner.JUnitRunner
import test.Help

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class BasicGetBehaviour extends Specification with Help {

	def checkZeros(length: Int) = {

		null
	}

	override def is =
		s2"""
Basic get behaviour:
	1kb data bytes									${checkZeros(1024)}
"""

	def put(sName: String, cName: String, key: String, mType: String = "text/plain")(

	): Future[DataEntry] = {

	}
}
