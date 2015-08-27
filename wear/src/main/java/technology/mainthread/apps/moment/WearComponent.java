package technology.mainthread.apps.moment;

import javax.inject.Singleton;

import dagger.Component;
import technology.mainthread.apps.moment.data.db.DatabaseModule;

@Singleton
@Component(modules = {MomentWearAppModule.class, DatabaseModule.class})
public interface WearComponent extends WearGraph {

    public final static class Initializer {
        public static WearComponent init(MomentWearApp app) {
            return DaggerWearComponent.builder()
                    .momentWearAppModule(new MomentWearAppModule(app))
                    .databaseModule(new DatabaseModule(app))
                    .build();
        }

        private Initializer() {
        } // No instances.
    }

}
