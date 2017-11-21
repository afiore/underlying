# Underlying

Boilerplate-free newtype serialisation

## Introduction

Wrapping primitive types into a single field case classes - elsewhere known as [newtypes](https://wiki.haskell.org/Newtype) -
is a popular idiom in Scala. This helps preventing bugs where multiple arguments
of the same type are supplied in the wrong order, with the added benefit of
making our code a bit more expressive and self-explanatory.

However, with such benefit comes some extra boilerplate, especially around 
serialisation/deserialisation logic.

```scala
import io.circe.{Encoder, Decoder, Json}
case class Id(value: String)
case class Document(id: Id, title: String)

implicit val idEnc: Encoder[Id] = Encoder.instance[Id](id => Json.fromString(id.value))
implicit val idDec: Decoder[Id] = Decoder.instance[Id](c => c.as[String].map(Id(_)))
```

Circe, as well as most JSON libraries written in Scala, in fact does not have a way to safely distinguish a
new type from a case class with one single field, hence it serialises `Id` as 
follows:



```scala
import io.circe.generic.semiauto._
implicit val idEnc: Encoder[Id] = deriveEncoder[Id]
implicit val docDec: Encoder[Document] = deriveEncoder[Document]
```
```scala
scala> val doc = Document(Id("xyz18a"),"Some doc")
doc: Document = Document(Id(xyz18a),Some doc)

scala> doc.asJson 
res1: io.circe.Json =
{
  "id" : {
    "value" : "xyz18a"
  },
  "title" : "Some doc"
}
```

Most of the times, this is not what we want!

## Usage

You can install _underlying_ by adding the following to your `build.sbt` file:

```scala
resolvers += Resolver.bintrayRepo("afiore","maven")

libraryDependencies += Seq(
  "com.github.afiore" %% "underlying-core" % "0.2.1",
  "com.github.afiore" %% "underlying-circe" % "0.2.1"
)
```

*Warning:* this library is an experiment and its usage in a production environment
is not encouraged. Moreover, it might be superseded once [this pull request](https://github.com/circe/circe/issues/469) is merged
in Circe

### Automatic derivation

Underlying provides a mechanism to get rid of some of this boilerplate:

```scala
import io.circe.{Encoder, Decoder, Json}
import io.circe.syntax._
import io.circe.generic.semiauto._
import underlying.circe.auto._

case class Id(value: String) extends AnyVal
case class Document(id: Id, title: String)

implicit val documentEncoder: Encoder[Document] = deriveEncoder
implicit val documentDecoder: Decoder[Document] = deriveDecoder
```

By making `Id` a [value class](https://docs.scala-lang.org/overviews/core/value-classes.html) and importing `underlying.circe.auto._`, 
we can now leverage the compiler to automatically derive an encoder/decoder for 
`Document.Id`, automating the same trivial unwrapping and wrapping logic implemented 
before.

```scala
scala> val doc = Document(Id("xyz18a"),"Some doc")
doc: Document = Document(Id(xyz18a),Some doc)

scala> doc.asJson 
res4: io.circe.Json =
{
  "id" : "xyz18a",
  "title" : "Some doc"
}

scala> doc.asJson.as[Document]
res5: io.circe.Decoder.Result[Document] = Right(Document(Id(xyz18a),Some doc))
```

Extending `AnyVal` will constrain our type to have only one private member, allowing 
the compiler to avoid instantiating the class in some circumstances. At the same time,
this will leave the user in full control to define where wrapping/unwrapping is desired,
without interfering with the default behaviour of circe's automatic derivation.

```scala

sealed trait DocumentWorkflow

case class Draft(document: Document) extends DocumentWorkflow
case class Published(document: Document) extends DocumentWorkflow

implicit val docWorkflowEnc: Encoder[DocumentWorkflow] = deriveEncoder[DocumentWorkflow]
implicit val docWorkflowDec: Decoder[DocumentWorkflow] = deriveDecoder[DocumentWorkflow]

val draftDoc: DocumentWorkflow = Draft(Document(Id("xyz"), "draft doc"))
```

The JSON representation of `draftDoc` is in fact the default one Circe produces when
derivation an `Encoder`/`Decoder` for a sealed trait hierarchy.

```scala
scala> val json = draftDoc.asJson
json: io.circe.Json =
{
  "Draft" : {
    "document" : {
      "id" : "xyz",
      "title" : "draft doc"
    }
  }
}

scala> json.as[DocumentWorkflow]
res10: io.circe.Decoder.Result[DocumentWorkflow] = Right(Draft(Document(Id(xyz),draft doc)))
```

## Copyright and license

Underlying is licensed under the [MIT License](https://opensource.org/licenses/MIT) (the “License”); you may not use this software except in compliance with the License.
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
