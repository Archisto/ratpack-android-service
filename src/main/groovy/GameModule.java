import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class GameModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(GameService.class).in(Scopes.SINGLETON);
    bind(WinnerDbCommands.class).in(Scopes.SINGLETON);
    bind(WinnerEndpoint.class).in(Scopes.SINGLETON);
    bind(CounterDbCommands.class).in(Scopes.SINGLETON);
    bind(CounterEndpoint.class).in(Scopes.SINGLETON);
  }
}
