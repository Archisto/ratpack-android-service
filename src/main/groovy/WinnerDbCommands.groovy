import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.exec.Blocking

import static ratpack.rx.RxRatpack.observe
import static ratpack.rx.RxRatpack.observeEach

class WinnerDbCommands {

    private final Sql sql
    private static final HystrixCommandGroupKey hystrixCommandGroupKey = HystrixCommandGroupKey.Factory.asKey("sql-winnerdb")

    @Inject
    public WinnerDbCommands(Sql sql) {
        this.sql = sql
    }

    void createTables() {
        sql.execute("drop table if exists winners")
        sql.execute("create table winners (nickname varchar(13) primary key, prizetier int)")
    }

    rx.Observable<GroovyRowResult> getAll() {
        return new HystrixObservableCommand<GroovyRowResult>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("getAll"))) {

            @Override
            protected rx.Observable<GroovyRowResult> construct() {
                observeEach(Blocking.get {
                    sql.rows("select nickname, prizetier from winners order by nickname")
                })
            }

            @Override
            protected String getCacheKey() {
                return "db-winnerdb-all"
            }
        }.toObservable()
    }

    rx.Observable<String> insert(final String nickname, final long prizetier) {
        return new HystrixObservableCommand<String>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("insert"))) {

            @Override
            protected rx.Observable<List<Object>> construct() {
                observe(Blocking.get {
                    sql.executeInsert("insert into winners (nickname, prizetier) values ($nickname, $prizetier)")
                })
            }
        }.toObservable()
    }

    rx.Observable<GroovyRowResult> find(final String nickname) {
        return new HystrixObservableCommand<GroovyRowResult>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("find"))) {

            @Override
            protected rx.Observable<GroovyRowResult> construct() {
                observe(Blocking.get {
                    sql.firstRow("select prizetier from winners where nickname = $nickname")
                })
            }

            @Override
            protected String getCacheKey() {
                return "db-winnerdb-find-$nickname"
            }
        }.toObservable()
    }

    rx.Observable<Void> update(final String nickname, final long prizetier) {
        return new HystrixObservableCommand<Void>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("update"))) {

            @Override
            protected rx.Observable<Integer> construct() {
                observe(Blocking.get {
                    sql.executeUpdate("update winners set prizetier = $prizetier where nickname = $nickname")
                })
            }
        }.toObservable()
    }

    rx.Observable<Void> delete(final String nickname) {
        return new HystrixObservableCommand<Void>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("delete"))) {

            @Override
            protected rx.Observable<Integer> construct() {
                observe(Blocking.get {
                    sql.executeUpdate("delete from winners where nickname = $nickname")
                })
            }
        }.toObservable()
    }

}
