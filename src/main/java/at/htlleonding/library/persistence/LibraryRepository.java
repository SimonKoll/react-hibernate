package at.htlleonding.library.persistence;
///home/peter/src/dbi4/quarkus-hibernate-cmdline

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

// @Transactional
// https://quarkus.io/guides/transaction
// https://quarkus.io/guides/hibernate-orm
// Mark your CDI bean method as @Transactional and the EntityManager will enlist and flush at commit.
// Make sure to wrap methods modifying your database (e.g. entity.persist()) within a transaction.
// Marking a CDI bean method @Transactional will do that for you and make that method a transaction boundary.
// We recommend doing so at your application entry point boundaries like your REST endpoint controllers.

@ApplicationScoped
public class LibraryRepository {
    public static void printWithThreadId(String msg) {
        System.out.println(LocalDateTime.now() + " Thread: "+Thread.currentThread().getName() + " " + msg);
    }

    @Inject
    Mutiny.SessionFactory sf;

    public <T> Uni<T> add(T a) {
       printWithThreadId("LibraryRepository.add");
        return sf
                .withTransaction((s) -> s
                        .persist(a)
                        .replaceWith(a)
                        .invoke(() -> printWithThreadId("within sf.persist..."))
                        .onFailure().transform(e -> new LibraryRepositoryException(e))
                );
    }

    public Uni<List<Author>> getAuthors() {
        return sf.withTransaction(s -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s
                    .createQuery("select a from Author a", Author.class)
                    .getResultList();
                }
        );
    }

    Random rnd = new Random();
    public Uni<List<Author>> getAuthorsFail() {
        return sf.withTransaction(s -> {
                    var query = "select a from Author a";
                    if(rnd.nextBoolean()) query = "select XXX from Author a";
                    return s
                            .createQuery(query, Author.class)
                            .getResultList();

                }
        );
    }
}
