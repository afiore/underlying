# Underlying

A simple mechanism to abstract over the serialisation of "newtypes" in scala.

## Rationale

Wrapping primitive types into a single field case classes is a popular idiom 
in Scala. This helps preventing bugs where multiple arguments
of the same type are supplied in the wrong order, with the additional benefit of
making our code expressive and self-documenting.

However, one side effect of adopting such idiom is a certain increase in boilerplate,
especially around serialisation/deserialisation logic.

```scala
import io.circe.{Encoder, Decoder, Json}

case class Id(value: String)
case class Document(id: Id, title: String)

implicit val idEncoder = Encoder.instance[Id](id => Json.fromString(id.value))
implicit val idDecoder = Decoder.instance[Id](c => c.as[String].map(Id(_)))

// docEncoder, docDecoder definitions ...
```

## Usage

You can install underlying by adding the following to your `build.sbt` file:

```scala
resolvers += Resolver.bintrayRepo("afiore","maven")

libraryDependencies += Seq(
  "com.github.afiore" %% "underlying-core" % "0.1.2",
  "com.github.afiore" %% "underlying-circe" % "0.1.2"
)
```

### Automatic derivation

Underlying provides a mechanism to get rid of some of this boilerplate:

```scala
import io.circe.{Encoder, Decoder, Json}
import io.circe.syntax._
import io.circe.generic.semiauto._
import underlying.circe.auto._

case class Id(value: String)
case class Document(id: Id, title: String)

implicit val documentEncoder: Encoder[Document] = deriveEncoder
implicit val documentDecoder: Decoder[Document] = deriveDecoder
```

By importing `underlying.circe.auto._` the compiler will automatically derive an
encoder/decoder for `Document.Id`, automating the same trivial unwrapping and wrapping
logic implemented above.

```scala
scala> val doc = Document(Id("xyz18a"),"Some doc")
doc: Document = Document(Id(xyz18a),Some doc)

scala> doc.asJson 
res6: io.circe.Json =
{
  "id" : "xyz18a",
  "title" : "Some doc"
}

scala> doc.asJson.as[Document]
res7: io.circe.Decoder.Result[Document] = Right(Document(Id(xyz18a),Some doc))
```

### Semi-automatic derivation

Automatic derivation works fine as long as all your newtypes behave as illustrated. 
However, keep in mind that, because of scala [implicit resolution rules](https://docs.scala-lang.org/tutorials/FAQ/finding-implicits.html),
the implicits in `underlying.circe.auto` will take priority over the ones you define in companion objects.
As an alternative to the fully automatic mechanism, the library also provides a semi automatic one
whereby encoders/decoders are derived explicitly:

```scala
import io.circe.{Encoder, Decoder, DecodingFailure, Json}
import io.circe.syntax._
import io.circe.generic.semiauto._
import underlying.circe.semiauto._

case class Id(value: String)
case class Version(toInt: Int)
object Version {
  implicit val versionEnc = deriveUnderlyingEncoder[Version]
  implicit val versionDec = implicitly[Decoder[Int]].flatMap { v =>
    Decoder.instance[Version] { hc =>
      if (v > 0) Right(Version(v))
      else Left(DecodingFailure("Version must be positive", hc.history))
    }
  }
}

object Id {
  implicit val idEnc = deriveUnderlyingEncoder[Id]
  implicit val idDec = deriveUnderlyingDecoder[Id]
}

case class Document(id: Id, version: Version, title: String)

implicit val documentEncoder: Encoder[Document] = deriveEncoder
```

While forcing the user to manually define implicits using `deriveUnderlyingEncoder`,
and `deriveUnderlyingDecoder`, the semi automatic mechanism allows for a higher
degree of control over implicits whereby newtypes can be either implemented or derived.
