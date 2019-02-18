import com.zaxxer.hikari.HikariConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.sql.SqlModule
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.handling.RequestLogger
import ratpack.hikari.HikariModule
import ratpack.hystrix.HystrixModule
import ratpack.rx.RxRatpack
import ratpack.service.Service
import ratpack.service.StartEvent

import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack

final Logger logger = LoggerFactory.getLogger(ratpack.class);

ratpack {
  bindings {
    module MarkupTemplateModule
    module HikariModule, { HikariConfig c ->
      c.addDataSourceProperty("url", System.getenv("JDBC_DATABASE_URL"))
      c.setDataSourceClassName("org.postgresql.ds.PGPoolingDataSource")
    }
    module SqlModule
    module GameModule
    module new HystrixModule().sse()

    bindInstance Service, new Service() {
      @Override
      void onStart(StartEvent event) throws Exception {
        logger.info "Initializing RX"
        RxRatpack.initialize()
        event.registry.get(GameService).createTables()
      }
    }
  }

  handlers { GameService gameService ->
    all RequestLogger.ncsa(logger)

    get {
      render groovyMarkupTemplate("index.gtpl", title: "Nappipeli")
    }

    get("hello") {
      response.send "Hello from Heroku!"
    }

    prefix("winners") {
      all chain(registry.get(WinnerEndpoint))
    }

    prefix("counters") {
      all chain(registry.get(CounterEndpoint))
    }

    files { dir "public" }
  }
}
