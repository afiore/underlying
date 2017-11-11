# Underlying

A simple mechanism to abstract over the serialisation of "newtypes" in scala.

## Rationale

Wrapping primitive types into a single field case classes - elsewhere known as [newtypes](https://wiki.haskell.org/Newtype) -
is a popular idiom in Scala. This helps preventing bugs where multiple arguments
of the same type are supplied in the wrong order, with the added benefit of
making our code a bit more expressive and self-explanatory.

However, with such benefit comes some extra boilerplate, especially around 
serialisation/deserialisation logic.

```tut:silent
import io.circe.{Encoder, Decoder, Json}
case class Id(value: String)
case class Document(id: Id, title: String)

implicit val idEnc: Encoder[Id] = Encoder.instance[Id](id => Json.fromString(id.value))
implicit val idDec: Decoder[Id] = Decoder.instance[Id](c => c.as[String].map(Id(_)))
```

Circe, our goto JSON library, in fact does not have a way to safely distinguish a
new type from a case class with one single field, hence it serialises our `Id` as 
follows:

```tut:invisible
import io.circe.{Encoder, Decoder, Json}
import io.circe.syntax._

case class Id(value: String)
case class Document(id: Id, title: String)
```
```tut:silent
import io.circe.generic.semiauto._
implicit val idEnc: Encoder[Id] = deriveEncoder[Id]
implicit val docDec: Encoder[Document] = deriveEncoder[Document]
```
```tut
val doc = Document(Id("xyz18a"),"Some doc")
doc.asJson 
```

Most of the times, this is not what we want!

## Usage

You can install underlying by adding the following to your `build.sbt` file:

```scala
resolvers += Resolver.bintrayRepo("afiore","maven")

libraryDependencies += Seq(
  "com.github.afiore" %% "underlying-core" % "0.1.4",
  "com.github.afiore" %% "underlying-circe" % "0.1.4"
)
```

### Automatic derivation

Underlying provides a mechanism to get rid of some of this boilerplate:

```tut:silent
import io.circe.{Encoder, Decoder, Json}
import io.circe.syntax._
import io.circe.generic.semiauto._
import underlying.generic.HasBaseTrait
import underlying.circe.auto._

case class Id(value: String)
case class Document(id: Id, title: String)

implicit val documentEncoder: Encoder[Document] = deriveEncoder
implicit val documentDecoder: Decoder[Document] = deriveDecoder
```

By importing `underlying.circe.auto._` the compiler will automatically derive an
encoder/decoder for `Document.Id`, automating the same trivial unwrapping and wrapping
logic implemented above.

```tut
val doc = Document(Id("xyz18a"),"Some doc")
doc.asJson 
doc.asJson.as[Document]
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
object Id {
  implicit val idEnc: Encoder[Id] = deriveUnderlyingEncoder[Id]
  implicit val idDec: Decoder[Id] = implicitly[Decoder[Int]].flatMap { intId =>
    Decoder.instance[Id] { c =>
      if (intId > 0) Right(Id(intId.toString))
      else Left(DecodingFailure("Id must be positive", c.history))
    }
  }
}

case class Document(id: Id, title: String)

implicit val documentEnc: Encoder[Document] = deriveEncoder
implicit val documentDec: Decoder[Document] = deriveDecoder
```

At the cost of forcing us to define an implicit val for each encoder/decoder instance,
the semi-automatic derivation provides a higher degree of control than the fully automatic one.
This is handy in situations where we want to automatically derive some instances while manually define others
(in the example above, we perform some basic validation within the `Decoder[Id]` instance).
