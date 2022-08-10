package tools.jackson.module.scala

import tools.jackson.databind.JacksonModule.SetupContext
import tools.jackson.databind.`type`.CollectionLikeType
import tools.jackson.databind.{BeanDescription, DeserializationConfig, JavaType, ValueDeserializer}
import tools.jackson.databind.deser.Deserializers
import tools.jackson.databind.jsontype.TypeDeserializer
import tools.jackson.module.scala.JacksonModule.InitializerBuilder
import tools.jackson.module.scala.deser.{ImmutableBitSetDeserializer, MutableBitSetDeserializer}

import scala.collection.{BitSet, immutable, mutable}

/**
 * Adds support for deserializing Scala [[scala.collection.BitSet]]s. Scala Bitsets can already be
 * serialized using [[IteratorModule]] or [[DefaultScalaModule]].
 * <p>
 * <b>Do not enable this module unless you are sure that no input is accepted from untrusted sources.</b>
 * </p>
 * Scala BitSets use memory based on the highest int value stored. So a BitSet with just one big int will use a lot
 * more memory than a Scala BitSet with many small ints stored in it.
 *
 * @since 2.14.0
 */
object BitSetDeserializerModule extends JacksonModule {
  override def getInitializers(config: ScalaModule.Config): Seq[SetupContext => Unit] = {
    val builder = new InitializerBuilder()
    builder += new Deserializers.Base {
      private val IMMUTABLE_BITSET_CLASS: Class[_] = classOf[immutable.BitSet]
      private val MUTABLE_BITSET_CLASS: Class[_] = classOf[mutable.BitSet]

      override def findCollectionLikeDeserializer(`type`: CollectionLikeType,
                                                  config: DeserializationConfig,
                                                  beanDesc: BeanDescription,
                                                  elementTypeDeserializer: TypeDeserializer,
                                                  elementDeserializer: ValueDeserializer[_]): ValueDeserializer[_] = {
        val rawClass = `type`.getRawClass
        if (IMMUTABLE_BITSET_CLASS.isAssignableFrom(rawClass)) {
          ImmutableBitSetDeserializer.asInstanceOf[ValueDeserializer[BitSet]]
        } else if (MUTABLE_BITSET_CLASS.isAssignableFrom(rawClass)) {
          MutableBitSetDeserializer.asInstanceOf[ValueDeserializer[BitSet]]
        } else {
          None.orNull
        }
      }

      override def hasDeserializerFor(deserializationConfig: DeserializationConfig, valueType: Class[_]): Boolean = {
        IMMUTABLE_BITSET_CLASS.isAssignableFrom(valueType) || MUTABLE_BITSET_CLASS.isAssignableFrom(valueType)
      }
    }
    builder.build()
  }
}
