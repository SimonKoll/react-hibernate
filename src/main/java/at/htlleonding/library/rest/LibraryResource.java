package at.htlleonding.library.rest;

import at.htlleonding.library.persistence.Author;
import at.htlleonding.library.persistence.LibraryRepository;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Path("/library")
public class LibraryResource {

    @Inject
    LibraryRepository repository;

    @GET
    @Path("authors")
    public Uni<List<AuthorDto>> getAuthors() {
        LibraryRepository.printWithThreadId("LibraryResource.getAuthors");
        return repository.getAuthors()
                .onItem().invoke(i -> LibraryRepository.printWithThreadId("getAuthors.onItem"))
                .onItem().transform(das -> das.stream().map(da -> new AuthorDto(da.getId(), da.getFirstName(), da.getLastName())).collect(Collectors.toList()));
    }

    @POST
    @Path("authors")
    public Uni<AuthorDto> postAuthor(AuthorDto dto) {
        var a = new Author(dto.FirstName, dto.LastName, LocalDate.now());
        return repository.add(a)
                .onItem().transform(da -> new AuthorDto(da.getId(), da.getFirstName(), da.getLastName()));
    }

    @GET
    @Path("fail")
    public Uni<List<AuthorDto>> getAuthorsFail() {
        LibraryRepository.printWithThreadId("LibraryResource.getAuthorsFail");
        var failResult = new ArrayList<AuthorDto>();
        failResult.add(new AuthorDto(-1, "Fail", "Fail"));
        return repository.getAuthorsFail()
                .onItem().invoke(i -> LibraryRepository.printWithThreadId("getAuthors.onItem"))
                .onItem().transform(das -> das.stream().map(da -> new AuthorDto(da.getId(), da.getFirstName(), da.getLastName())).collect(Collectors.toList()))
                .onFailure().recoverWithItem(failResult);
    }
}