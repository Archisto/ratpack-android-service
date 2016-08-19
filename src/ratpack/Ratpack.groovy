import com.zaxxer.hikari.HikariConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.sql.SqlModule
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.handling.RequestLogger
import ratpack.hikari.HikariModule

import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.fromJson;

final Logger logger = LoggerFactory.getLogger(ratpack.class);

ratpack {
  bindings {
    module MarkupTemplateModule
    module HikariModule, { HikariConfig c ->
      c.addDataSourceProperty("url", System.getenv("JDBC_DATABASE_URL"))
      c.setDataSourceClassName("org.postgresql.ds.PGPoolingDataSource")
    }
    module SqlModule
//    module new HystrixModule().sse()

//    bind BookErrorHandler


//    bindInstance Service, new Service() {
//      @Override
//      void onStart(StartEvent event) throws Exception {
//        logger.info "Initializing RX"
//        RxRatpack.initialize()
//        event.registry.get(BookService).createTable()
//      }
//    }
  }

  handlers {
    all RequestLogger.ncsa(logger)

    get {
      render groovyMarkupTemplate("index.gtpl", title: "My Ratpack App")
    }

    get("hello") {
      response.send "Hello from Heroku!"
    }

    prefix("books") {

      get {
        render(json([]))
      }

      post("new") {
        render(parse(fromJson(Book.class)).map{ b ->
          // TODO create record
          json(new Book(isbn: b.getIsbn()))
        })
      }

      get(":isbn") {
        def isbn = pathTokens["isbn"]

        // TODO get record

        render(json(new Book(isbn: isbn)))
      }
    }


    files { dir "public" }
  }
}
