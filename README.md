# Underlying

A simple mechanism to abstract over "newtypes" in scala.

## Rationale

Wrapping primitive types into a single field case classes is a popular idiom 
in Scala. Such idiom is useful in preventing bugs where multiple function arguments
of the same type are supplied in the wrong order, as well as in making code 
more expressive and self-documenting.

However, one side effect of adopting such idiom is a certain increase in boilerplate,
especially around serialisation/deserialisation logic.

```scala
import io.circe.{Encoder, Decoder, Json}

object Document {
  case class Id(value: String)
  
  object Id {
    implicit val idEncoder = Encoder.instance[Id](id => Json.fromString(id.value))
    implicit val idDecoder = Decoder.instance[Id](c => c.as[String].map(Id(_)))
  }
}
import Document._
case class Document(id: Id, title: String)
```




