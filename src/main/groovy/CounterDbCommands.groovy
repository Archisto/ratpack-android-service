import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.exec.Blocking

import static ratpack.rx.RxRatpack.observe
import static ratpack.rx.RxRatpack.observeEach

class CounterDbCommands {

    private final Sql sql
    private static final HystrixCommandGroupKey hystrixCommandGroupKey = HystrixCommandGroupKey.Factory.asKey("sql-counterdb")

    @Inject
    public CounterDbCommands(Sql sql) {
        this.sql = sql
    }

    void createTables() {
        sql.execute("drop table if exists counters")
        sql.execute("create table counters (value int)")
    }

    rx.Observable<GroovyRowResult> getAll() {
        return new HystrixObservableCommand<GroovyRowResult>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("getAll"))) {

            @Override
            protected rx.Observable<GroovyRowResult> construct() {
                observeEach(Blocking.get {
                    sql.rows("select value from counters order by value")
                })
            }

            @Override
            protected String getCacheKey() {
                return "db-counterdb-all"
            }
        }.toObservable()
    }

    rx.Observable<String> insert(final long value) {
        return new HystrixObservableCommand<String>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("insert"))) {

            @Override
            protected rx.Observable<List<Object>> construct() {
                observe(Blocking.get {
                    sql.executeInsert("insert into counters (value) values ($value)")
                })
            }
        }.toObservable()
    }

    rx.Observable<Void> update(final long value) {
        return new HystrixObservableCommand<Void>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("update"))) {

            @Override
            protected rx.Observable<Integer> construct() {
                observe(Blocking.get {
                    sql.executeUpdate("update counters set value = $value")
                })
            }
        }.toObservable()
    }

    rx.Observable<Void> delete() {
        return new HystrixObservableCommand<Void>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("delete"))) {

            @Override
            protected rx.Observable<Integer> construct() {
                observe(Blocking.get {
                    sql.executeUpdate("delete from counters")
                })
            }
        }.toObservable()
    }

}
