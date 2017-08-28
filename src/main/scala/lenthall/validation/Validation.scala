package lenthall.validation

import java.net.{URI, URL}

import cats.data.NonEmptyList
import cats.data.Validated._
import cats.syntax.option._
import cats.syntax.validated._
import lenthall.validation.ErrorOr.ErrorOr
import net.ceedubs.ficus.readers.{StringReader, ValueReader}
import org.slf4j.Logger

import scala.util.{Failure, Success, Try}

object Validation {
  def warnNotRecognized(keys: Set[String], reference: Set[String], context: String, logger: Logger): Unit = {
    val unrecognizedKeys = keys.diff(reference)
    if (unrecognizedKeys.nonEmpty) {
      logger.warn(s"Unrecognized configuration key(s) for $context: ${unrecognizedKeys.mkString(", ")}")
    }
  }
  
  implicit val urlReader: ValueReader[URL] = StringReader.stringValueReader.map { URI.create(_).toURL }
  
  def validate[A](block: => A): ErrorOr[A] = Try(block) match {
    case Success(result) => result.validNel
    case Failure(f) => f.getMessage.invalidNel
  }

  implicit class TryValidation[A](val t: Try[A]) extends AnyVal {
    def toErrorOr: ErrorOr[A] = {
      fromTry(t).leftMap(_.getMessage).toValidatedNel[String, A] 
    }
  }

  implicit class OptionValidation[A](val o: Option[A]) extends AnyVal {
    def toErrorOr(message: => String): ErrorOr[A] = {
      o.toValid(NonEmptyList.of(message))
    }
  }
}
