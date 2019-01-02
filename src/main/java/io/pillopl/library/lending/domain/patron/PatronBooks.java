package io.pillopl.library.lending.domain.patron;


import io.pillopl.library.lending.domain.book.AvailableBook;
import io.pillopl.library.lending.domain.book.BookOnHold;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollectedByPatron;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollectingFailed;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldFailed;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldByPatron;
import io.pillopl.library.lending.domain.book.BookId;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.pillopl.library.lending.domain.patron.Reason.withReason;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static java.util.Collections.emptySet;

//TODO - rename to patron?
@AllArgsConstructor
@EqualsAndHashCode(of = "patron")
public class PatronBooks {

    private final PatronInformation patron;

    private final List<PlacingOnHoldPolicy> placingOnHoldPolicies;

    private final OverdueCheckouts overdueCheckouts;

    private final PatronHolds patronHolds;

    public Either<BookHoldFailed, BookPlacedOnHoldByPatron> placeOnHold(AvailableBook book) {
       return placeOnHold(book, HoldDuration.forOpenEnded());
    }

    public Either<BookHoldFailed, BookPlacedOnHoldByPatron> placeOnHold(AvailableBook aBook, HoldDuration forDuration) {
        Option<Rejection> rejection = tryPlacingOnHold(aBook, forDuration);
        if (!rejection.isEmpty()) {
            return left(BookHoldFailed.now(rejection.get().getReason(), aBook.getBookId(), aBook.getLibraryBranch(), patron));
        }
        return right(BookPlacedOnHoldByPatron.now(aBook.getBookInformation(), aBook.getLibraryBranch(), patron, forDuration));
    }

    public Either<BookCollectingFailed, BookCollectedByPatron> collect(BookOnHold book) {
        if (patronHolds.doesNotContain(book)) {
            return left(BookCollectingFailed.now(withReason("book is not on hold by patron"), book.getBookId(), book.getHoldPlacedAt(), patron));
        }
        return right(BookCollectedByPatron.now(book.getBookInformation(), book.getHoldPlacedAt(), patron.getPatronId()));
    }

    private Option<Rejection> tryPlacingOnHold(AvailableBook aBook, HoldDuration forDuration) {
        return placingOnHoldPolicies
                .toStream()
                .map(policy -> policy.apply(aBook, this, forDuration))
                .find(Either::isLeft)
                .map(Either::getLeft);
    }

    boolean isRegular() {
        return patron.isRegular();
    }

    int overdueCheckoutsAt(LibraryBranchId libraryBranch) {
        return overdueCheckouts.countAt(libraryBranch);
    }

    int numberOfHolds() {
        return patronHolds.count();
    }

}

@Value
//TODO add not null
class OverdueCheckouts {

    Map<LibraryBranchId, Set<BookId>> overdueCheckouts;

    static OverdueCheckouts noOverdueCheckouts() {
        return new OverdueCheckouts(new HashMap<>());
    }

    int countAt(LibraryBranchId libraryBranchId) {
        return overdueCheckouts.getOrDefault(libraryBranchId, emptySet()).size();
    }

}


