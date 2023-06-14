package at.specure.di

import android.content.Context
import at.specure.androidX.Application
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * Main Application component that wires all application modules together
 */
@Singleton
@Component(
    modules = [AppModule::class]
)
interface AppComponent {

    fun inject(app: Application)

    @Component.Builder
    abstract class Builder {

        fun context(context: Context): Builder {
            seedContext(context)
            return this
        }

        @BindsInstance
        abstract fun seedContext(context: Context)

        abstract fun build(): AppComponent
    }
}