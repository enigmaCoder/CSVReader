package com.enigmatech.csvreader.module

import com.enigmatech.csvreader.impl.RenderEngineRegistry
import com.enigmatech.csvreader.formatter.JsonFormatter
import com.enigmatech.csvreader.formatter.XmlFormatter
import com.enigmatech.csvreader.formatter.YamlFormatter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FormatterModule {

    @Provides
    @Singleton
    fun provideJsonFormatter(): JsonFormatter = JsonFormatter()

    @Provides
    @Singleton
    fun provideXmlFormatter(): XmlFormatter = XmlFormatter()

    @Provides
    @Singleton
    fun provideYamlFormatter(): YamlFormatter = YamlFormatter()

    @Provides
    @Singleton
    fun provideFormatterRegistry(
        jsonFormatter: JsonFormatter,
        xmlFormatter: XmlFormatter,
        yamlFormatter: YamlFormatter
    ): RenderEngineRegistry {
        return RenderEngineRegistry().apply {
            register("json", jsonFormatter)
            register("xml", xmlFormatter)
            register("yaml", yamlFormatter)
        }
    }
}
