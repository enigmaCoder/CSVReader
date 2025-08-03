package com.enigmatech.csvreader.module

import com.enigmatech.csvreader.impl.DefaultNormalizer
import com.enigmatech.csvreader.impl.DefaultTransformer
import com.enigmatech.csvreader.impl.ISchemaParser
import com.enigmatech.csvreader.impl.Normalizer
import com.enigmatech.csvreader.impl.SchemaParserImpl
import com.enigmatech.csvreader.impl.Transformer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SchemaParserModule {
    @Provides
    @Singleton
    fun provideSchemaParser(): ISchemaParser {
        return SchemaParserImpl()
    }

    @Provides
    @Singleton
    fun provideTransformer(): Transformer = DefaultTransformer()

    @Provides
    @Singleton
    fun provideNormalizer(transformer: Transformer): Normalizer = DefaultNormalizer(transformer)
}