import ratpack.groovy.handling.GroovyChainAction

import javax.inject.Inject

import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.jsonNode

class WinnerEndpoint extends GroovyChainAction {

  private final GameService gameService

  @Inject
  WinnerEndpoint(GameService gameService) {
    this.gameService = gameService
  }

  @Override
  void execute() throws Exception {
    post("new") {
      parse(jsonNode()).
              observe().
              flatMap { input ->
                gameService.insert(
                        input.get("nickname").asText(),
                        input.get("prizetier").asInt()
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

    path(":nickname") {
      def nickname = pathTokens["nickname"]

      byMethod {
        get {
          gameService.find(nickname).
              single().
              subscribe { Winner winner ->
            if (winner == null) {
              clientError 404
            } else {
              render json(winner)
            }
          }
        }
        put {
          parse(jsonNode()).
              observe().
              flatMap { input ->
              gameService.update(
                  nickname,
                  0l
              )
          }.
          flatMap {
            gameService.find(nickname)
          }.
          single().
              subscribe { Winner winner ->
            render json(winner)
          }
        }
        delete {
          gameService.delete(nickname).
              subscribe {
            response.send()
          }
        }
      }
    }

    all {
      byMethod {
        get {
          gameService.all().
                  toList().
                  subscribe { List<Winner> winners ->
                    render json(winners)
                  }
        }
      }
    }

  }
}
