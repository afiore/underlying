package underlying.circe.auto

import io.circe.{Decoder, Encoder, Json}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FreeSpec, Matchers}
import shapeless.test.illTyped

class AutoTest extends FreeSpec with TypeCheckedTripleEquals with Matchers {
  "circe generics" - {
    case class Id(value: String)
    object OtherId {
      implicit val otherIdEnc: Encoder[OtherId] =
        Encoder.instance(id => Json.fromString(s"other-id:$id"))
    }
    case class OtherId(toInt: Int)

    case class TwoFields(a: Char, b: Int)

    "auto-derive Encoder" in {
      val enc = implicitly[Encoder[Id]]
      enc(Id("some-id")) should ===(Json.fromString("some-id"))
    }
    "auto-derive Decoder" in {
      Json.fromString("some-id").as[Id] should ===(Right(Id("some-id")))
    }
    "discards companion object implicits!" in {
      implicitly[Encoder[OtherId]] should !==(OtherId.otherIdEnc)
    }
    "does not derive Encoder for classes with more than one field" in {
      illTyped { "implicitly[Encoder[TwoFields]]" }
    }
    "does not derive Decoder for classes with more than one field" in {
      illTyped { "implicitly[Decoder[TwoField]]" }
    }
  }

  "io.circe.generic.semiauto integration" - {
    import io.circe.generic.semiauto._
    import Json._

    object Document {
      case class Id(value: String)
      case class Version(toInt: Int)
      case class MarkdownBody(value: String)
      case class Section(heading: String, body: MarkdownBody)

      implicit val docEnc: Encoder[Document] =
        deriveEncoder[Document]

      implicit val docDec: Decoder[Document] =
        deriveDecoder[Document]

      object Section {
        implicit val sectionEnc: Encoder[Section] = deriveEncoder[Section]
        implicit val sectionDec: Decoder[Section] = deriveDecoder[Section]
      }
    }

    import Document._
    case class Document(id: Id,
                        version: Version,
                        title: String,
                        sections: List[Section])

    val doc =
      Document(Id("x"),
               Version(1),
               "test doc",
               List(
                 Section("section 1", MarkdownBody("blurb 1")),
                 Section("section 2", MarkdownBody("blurb 2"))
               ))

    val jsonDoc =
      obj(
        "id"      -> fromString("x"),
        "version" -> fromInt(1),
        "title"   -> fromString("test doc"),
        "sections" -> arr(
          obj(
            "heading" -> fromString("section 1"),
            "body"    -> fromString("blurb 1")
          ),
          obj(
            "heading" -> fromString("section 2"),
            "body"    -> fromString("blurb 2")
          )
        )
      )

    "encode/decode round-trip" in {
      import Document._
      import io.circe.syntax._

      doc.asJson should ===(jsonDoc)
      jsonDoc.as[Document] should ===(Right(doc))
    }
  }
}
