package underlying.circe.auto

import io.circe.{Decoder, Encoder, Json}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FreeSpec, Matchers}
import shapeless.test.illTyped
import underlying.NewType
import io.circe.generic.semiauto.deriveDecoder

class AutoTest extends FreeSpec with TypeCheckedTripleEquals with Matchers {
  "circe generics" - {

    case class Id(value: String) extends NewType[String]
    object OtherId {
      implicit val otherIdEnc: Encoder[OtherId] =
        Encoder.instance(id => Json.fromString(s"other-id:$id"))
    }
    case class OtherId(value: Int) extends NewType[Int]

    sealed trait X
    case class ExtendsX(a: Int) extends X

    case class TwoFields(a: Char, b: Int)

    "auto-derives Encoder" in {
      val enc = implicitly[Encoder[Id]]
      enc(Id("some-id")) should ===(Json.fromString("some-id"))
    }
    "auto-derives Decoder" in {
      Json.fromString("some-id").as[Id] should ===(Right(Id("some-id")))
    }
    "does not derive Encoder for classes with more than one field" in {
      illTyped { "implicitly[Encoder[TwoFields]]" }
    }
    "does not derive Encoder for classes that extend a sealed trait" in {
      illTyped { "implicitly[Encoder[ExtendX]]" }
    }

    "does not derive Decoder for classes with more than one field" in {
      illTyped { "implicitly[Decoder[TwoField]]" }
    }
    "does not derive Decoder for classes that extend a sealed trait" in {
      illTyped { "implicitly[Decoder[ExtendsX]]" }
    }
    "discards companion object implicits!" in {
      implicitly[Encoder[OtherId]] should !==(OtherId.otherIdEnc)
    }
  }

  "io.circe.generic.semiauto integration" - {
    import io.circe.generic.semiauto._
    import io.circe.syntax._
    import Json._

    object Document {
      case class Id(value: String)           extends NewType[String]
      case class Version(value: Int)         extends NewType[Int]
      case class MarkdownBody(value: String) extends NewType[String]
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
//
    import Document._
    case class Document(id: Id,
                        version: Version,
                        title: String,
                        sections: List[Section])

    // One fields ADTs: shouldn't be treated as newtypes
    sealed trait DocumentWithStatus {
      def document: Document
    }
    object DocumentWithStatus {
      case class Draft(document: Document)     extends DocumentWithStatus
      case class Published(document: Document) extends DocumentWithStatus

      implicit val docWithStatusEnc: Encoder[DocumentWithStatus] =
        deriveEncoder[DocumentWithStatus]

      implicit val docWithStatusDec: Decoder[DocumentWithStatus] =
        deriveDecoder[DocumentWithStatus]
    }
//
    val doc =
      Document(Id("x"),
               Version(1),
               "test doc",
               List(
                 Section("section 1", MarkdownBody("blurb 1")),
                 Section("section 2", MarkdownBody("blurb 2"))
               ))

    val draftDoc: DocumentWithStatus = DocumentWithStatus.Draft(doc)

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

    val jsonDraftDoc = obj(
      "Draft" -> obj(
        "document" -> jsonDoc
      )
    )

    "encode/decode round-trip" in {
      doc.asJson should ===(jsonDoc)
      jsonDoc.as[Document] should ===(Right(doc))
    }

    "distinguishes one field ADTs from case classes" in {
      draftDoc.asJson should ===(jsonDraftDoc)
      jsonDraftDoc.as[DocumentWithStatus] should ===(Right(draftDoc))
    }
  }
}
