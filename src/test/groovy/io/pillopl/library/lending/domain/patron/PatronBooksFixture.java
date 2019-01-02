package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronInformation.PatronType;
import io.vavr.collection.List;

import java.util.*;
import java.util.stream.Collectors;

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId;
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch;
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular;
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Researcher;
import static io.pillopl.library.lending.domain.patron.PlacingOnHoldPolicy.*;
import static java.util.stream.IntStream.rangeClosed;

public class PatronBooksFixture {

    public static PatronBooks regularPatron() {
        return regularPatron(anyPatronId());
    }

    public static PatronBooks regularPatronWithPolicy(PlacingOnHoldPolicy placingOnHoldPolicy) {
        return patronWithPolicy(anyPatronId(), Regular, placingOnHoldPolicy);
    }

    public static PatronBooks researcherPatronWithPolicy(PlacingOnHoldPolicy placingOnHoldPolicy) {
        return patronWithPolicy(anyPatronId(), Researcher, placingOnHoldPolicy);
    }

    public static PatronBooks regularPatronWithPolicy(PatronId patronId, PlacingOnHoldPolicy placingOnHoldPolicy) {
        return patronWithPolicy(patronId, Regular, placingOnHoldPolicy);
    }

    public static PatronBooks researcherPatronWithPolicy(PatronId patronId, PlacingOnHoldPolicy placingOnHoldPolicy) {
        return patronWithPolicy(patronId, Researcher, placingOnHoldPolicy);
    }

    private static PatronBooks patronWithPolicy(PatronId patronId, PatronType type, PlacingOnHoldPolicy placingOnHoldPolicy) {
        return new PatronBooks(patronInformation(patronId, type),
                List.of(placingOnHoldPolicy),
                OverdueCheckouts.noOverdueCheckouts(),
                noHolds());
    }

    public static PatronBooks regularPatron(PatronId patronId) {
        return new PatronBooks(
                patronInformation(patronId, Regular),
                List.of(onlyResearcherPatronsCanHoldRestrictedBooksPolicy),
                OverdueCheckouts.noOverdueCheckouts(),
                noHolds());
    }

    public static PatronBooks researcherPatron(PatronId patronId) {
        return new PatronBooks(
                patronInformation(patronId, Researcher),
                List.of(onlyResearcherPatronsCanHoldRestrictedBooksPolicy),
                OverdueCheckouts.noOverdueCheckouts(),
                noHolds());
    }

    static PatronInformation patronInformation(PatronId id, PatronType type) {
        return new PatronInformation(id, type);
    }

    public static PatronBooks regularPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronBooks(
                patronInformation(patronId, Regular),
                List.of(regularPatronMaximumNumberOfHoldsPolicy),
                OverdueCheckouts.noOverdueCheckouts(),
                booksOnHold(numberOfHolds));
    }

    static PatronBooks regularPatronWith(PatronHold patronHold) {
        PatronId patronId = anyPatronId();
        PatronHolds patronHolds = new PatronHolds(Collections.singleton(patronHold));
        return new PatronBooks(
                patronInformation(patronId, Regular),
                allCurrentPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                patronHolds);
    }

    public static PatronHold onHold() {
        return new PatronHold(anyBookId(), anyBranch());
    }

    static PatronHolds booksOnHold(int numberOfHolds) {
        return new PatronHolds(rangeClosed(1, numberOfHolds)
                .mapToObj(i -> new PatronHold(anyBookId(), anyBranch()))
                .collect(Collectors.toSet()));
    }

    static PatronBooks researcherPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronBooks(
                patronInformation(patronId, Researcher),
                List.of(regularPatronMaximumNumberOfHoldsPolicy),
                OverdueCheckouts.noOverdueCheckouts(),
                booksOnHold(numberOfHolds));
    }

    static PatronBooks regularPatronWithOverdueCheckouts(LibraryBranchId libraryBranchId, Set<BookId> overdueResources) {
        Map<LibraryBranchId, Set<BookId>> overdueCheckouts = new HashMap<>();
        overdueCheckouts.put(libraryBranchId, overdueResources);
        return new PatronBooks(
                patronInformation(anyPatronId(), Regular),
                List.of(overdueCheckoutsRejectionPolicy),
                new OverdueCheckouts(overdueCheckouts),
                noHolds());
    }

    public static PatronId anyPatronId() {
        return patronId(UUID.randomUUID());
    }

    static PatronId patronId(UUID patronId) {
        return new PatronId(patronId);
    }

    static PatronHolds noHolds() {
        return new PatronHolds(new HashSet<>());
    }



}
