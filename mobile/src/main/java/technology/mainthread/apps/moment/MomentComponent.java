package technology.mainthread.apps.moment;

import javax.inject.Singleton;

import dagger.Component;
import technology.mainthread.apps.moment.data.DataModule;
import technology.mainthread.apps.moment.data.db.DatabaseModule;
import technology.mainthread.apps.moment.module.EndpointModule;

@Singleton
@Component(modules = {MomentAppModule.class, DataModule.class, EndpointModule.class, DatabaseModule.class})
public interface MomentComponent extends MomentGraph {

    final class Initializer {
        public static MomentComponent init(MomentApp app) {
            return DaggerMomentComponent.builder()
                    .momentAppModule(new MomentAppModule(app))
                    .dataModule(new DataModule(app))
                    .endpointModule(new EndpointModule(app, app.getResources()))
                    .databaseModule(new DatabaseModule(app))
                    .build();
        }

        private Initializer() {
        } // No instances.
    }

}
