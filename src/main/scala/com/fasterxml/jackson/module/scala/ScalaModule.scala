package com.fasterxml.jackson.module.scala

import com.fasterxml.jackson.databind.JacksonModule.SetupContext
import com.fasterxml.jackson.module.scala.deser.{ScalaNumberDeserializersModule, UntypedObjectDeserializerModule}
import com.fasterxml.jackson.module.scala.introspect.ScalaAnnotationIntrospectorModuleInstance

object ScalaModule {

  trait Config {
    def shouldApplyDefaultValuesWhenDeserializing(): Boolean
  }

  class Builder extends Config {
    private val initializers = Seq.newBuilder[SetupContext => Unit]
    private var applyDefaultValuesWhenDeserializing = true

    def applyDefaultValuesWhenDeserializing(applyDefaultValues: Boolean): Builder = {
      applyDefaultValuesWhenDeserializing = applyDefaultValues
      this
    }

    override def shouldApplyDefaultValuesWhenDeserializing(): Boolean = applyDefaultValuesWhenDeserializing

    def addModule(module: JacksonModule): Builder = {
      module.initializers.result().foreach(init => initializers += init)
      this
    }

    def addAllBuiltinModules(): Builder = {
      addModule(IteratorModule)
      addModule(EnumerationModule)
      addModule(OptionModule)
      addModule(SeqModule)
      addModule(IterableModule)
      addModule(TupleModule)
      addModule(MapModule)
      addModule(SetModule)
      addModule(ScalaNumberDeserializersModule)
      addModule(new ScalaAnnotationIntrospectorModuleInstance(this))
      addModule(UntypedObjectDeserializerModule)
      addModule(EitherModule)
      addModule(new SymbolModuleInstance(this))
      this
    }

    def build(): JacksonModule = {
      val configInstance = this
      val module = new JacksonModule {
        override val config = configInstance
      }
      initializers.result().foreach(init => module += init)
      module
    }
  }

  def builder(): Builder = new Builder()

  val defaultBuilder: Config = builder()
}
