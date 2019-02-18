import ratpack.groovy.handling.GroovyChainAction

import javax.inject.Inject

import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.jsonNode

class CounterEndpoint extends GroovyChainAction {

  private final GameService gameService

  @Inject
  CounterEndpoint(GameService gameService) {
    this.gameService = gameService
  }

  @Override
  void execute() throws Exception {
    post("new") {
      parse(jsonNode()).
              observe().
              flatMap { input ->
                gameService.insertCounter(
                        input.get("value")
                )
              }.
              single().
              flatMap { value ->
                gameService.findCounter(value)
              }.
              single().
              subscribe { Counter createdCounter ->
                render json(createdCounter)
              }
    }

    post("update") {
      parse(jsonNode()).
              observe().
              flatMap { input ->
                gameService.updateCounter(
                        input.get("value")
                )
              }.
              single().
              flatMap { value ->
                gameService.findCounter(value)
              }.
              single().
              subscribe { Counter createdCounter ->
                render json(createdCounter)
              }
    }

    all {
      byMethod {
        get {
          gameService.allCounters().
                  toList().
                  subscribe { List<Counter> counters ->
                    render json(counters)
                  }
        }
      }
    }

  }
}
