import groovy.sql.GroovyRowResult
import groovy.util.logging.Slf4j
import rx.Observable

import javax.inject.Inject

@Slf4j
class GameService {

    private final WinnerDbCommands winnerDbCommands

    @Inject
    GameService(WinnerDbCommands winnerDbCommands) {
        this.winnerDbCommands = winnerDbCommands
    }

    void createTable() {
        log.info("Creating database tables")
        winnerDbCommands.createTables()
    }

    Observable<Winner> all() {
        winnerDbCommands.getAll().map { row ->
            new Winner(
                    row.nickname,
                    row.prizetier
            )
        }
    }

    Observable<String> insert(String nickname, long prizetier) {
        winnerDbCommands.insert(nickname, prizetier).
                map {
                    nickname
                }
    }

    Observable<Winner> find(String nickname) {
        winnerDbCommands.find(nickname).map { GroovyRowResult dbRow ->
            return new Winner(
                    nickname,
                    dbRow.prizetier
            )
        }
    }

    Observable<Void> update(String nickname, long prizetier) {
        winnerDbCommands.update(nickname, prizetier)
    }

    Observable<Void> delete(String nickname) {
        winnerDbCommands.delete(nickname)
    }

    Observable<Winner> allCounters() {
        counterDbCommands.getAll().map { row ->
            new Counter(
                    row.value
            )
        }
    }

    Observable<String> insertCounter(long value) {
        counterDbCommands.insert(value).
                map {
                    value
                }
    }

    Observable<Void> updateCounter(long newValue) {
        counterDbCommands.update(newValue)
    }
}