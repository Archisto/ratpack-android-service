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
    post("raise") {
      parse(jsonNode()).
              observe().
              flatMap { input ->
                gameService.insert(
                        input.get("nickname").asText(),
                        0l
                )
              }.
              single().
              flatMap { nickname ->
                gameService.find(nickname)
              }.
              single().
              subscribe { Winner createdWinner ->
                render json(createdWinner)
              }
    }
  }
}
