package at.htlleonding.persistence;

import at.htlleonding.library.persistence.Author;
import at.htlleonding.library.persistence.LibraryRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.*;
import org.wildfly.common.Assert;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;

@QuarkusTest
public class LibraryRepositoryTest {
    @Inject
    LibraryRepository target;

    Integer integerResult = -1;
    Author authorResult = null;
    Boolean hasFailed = false;

    @Test
    public void insertAuthor_isPersisted_entityHasIdSet() throws InterruptedException {
        var a1 = new Author();
        a1.setDateDeath(LocalDate.of(1980, 01, 01));
        a1.setDateBirth(LocalDate.of(1940, 01, 01));
        a1.setFirstName("George");
        a1.setLastName("Orwell");

        Author result = target
                .add(a1)
                .onItem().invoke((a) -> LibraryRepository.printWithThreadId("target.onItem: " + a.toString()) )
                .await().atMost(Duration.ofMinutes(5));

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getId());
    }

    Throwable libraryRepositoryException = null;
    @Test
    public void insertNull_triggersFailure() throws InterruptedException {
        Author a1 = null;
        authorResult = Author.JohnDoe;

        var result = target
                .add(a1)
                .onFailure().recoverWithItem((f) -> { libraryRepositoryException = f; hasFailed = true; return null; });

        authorResult = result.await().atMost(Duration.ofMinutes(5));

        Assert.assertTrue(authorResult == null);
        Assert.assertTrue(hasFailed);
        Assert.assertNotNull(libraryRepositoryException);
    }
}
