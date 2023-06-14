package at.specure.androidX

import at.specure.androidX.Application
import at.specure.di.AppComponent

/**
 * Keeps and delegates all calls to [AppComponent]
 */
object Injector : AppComponent {

    lateinit var component: AppComponent

    override fun inject(app: Application) = component.inject(app)
}