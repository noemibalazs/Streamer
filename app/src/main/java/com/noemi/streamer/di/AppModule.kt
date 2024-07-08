package com.noemi.streamer.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import com.noemi.streamer.ktor.KtorDataSource
import com.noemi.streamer.ktor.KtorDataSourceImpl
import com.noemi.streamer.networkconnection.ConnectionService
import com.noemi.streamer.networkconnection.ConnectionServiceImpl
import com.noemi.streamer.repository.PayloadRepository
import com.noemi.streamer.repository.PayloadRepositoryImpl
import com.noemi.streamer.room.PayloadAccountConverter
import com.noemi.streamer.room.PayloadDAO
import com.noemi.streamer.room.PayloadDataBase
import com.noemi.streamer.util.BASE_URL
import com.noemi.streamer.util.PAYLOAD_DB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun providesPayloadDataBase(
        @ApplicationContext context: Context,
        payloadAccountConverter: PayloadAccountConverter
    ): PayloadDataBase = Room.databaseBuilder(context, PayloadDataBase::class.java, PAYLOAD_DB)
        .addTypeConverter(payloadAccountConverter)
        .fallbackToDestructiveMigration()
        .build()


    @Provides
    @Singleton
    fun providesPayloadDao(payloadDataBase: PayloadDataBase): PayloadDAO = payloadDataBase.getPayloadDao()


    @Provides
    @Singleton
    fun providesRepository(payloadDAO: PayloadDAO): PayloadRepository = PayloadRepositoryImpl(payloadDAO)


    @Singleton
    @Provides
    fun provideScope(): CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Provides
    @Singleton
    fun providesActivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    @Provides
    @Singleton
    fun providesConnectionService(
        connectivityManager: ConnectivityManager,
        scope: CoroutineScope
    ): ConnectionService = ConnectionServiceImpl(connectivityManager, scope)

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun providesJson() = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Singleton
    @Provides
    fun providesKtorClient(json: Json): HttpClient = HttpClient(Android.create()) {
        defaultRequest {
            url(BASE_URL)
        }

        install(Logging) {
            level = LogLevel.ALL
        }

        install(ContentNegotiation) {
            json(json)
        }
    }

    @Provides
    @Singleton
    fun providesKtorDataSource(client: HttpClient, json: Json): KtorDataSource = KtorDataSourceImpl(client, json)
}