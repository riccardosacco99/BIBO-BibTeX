package it.riccardosacco.bibobibtex.model.bibo;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;

public final class BiboPublicationDate {
    private final int year;
    private final Integer month;
    private final Integer day;

    private BiboPublicationDate(int year, Integer month, Integer day) {
        this.year = validateYear(year);
        this.month = validateMonth(month);
        this.day = validateDay(day);
        if (this.day != null && this.month == null) {
            throw new IllegalArgumentException("day requires month");
        }
    }

    public static BiboPublicationDate ofYear(int year) {
        return new BiboPublicationDate(year, null, null);
    }

    public static BiboPublicationDate ofYearMonth(int year, int month) {
        return new BiboPublicationDate(year, month, null);
    }

    public static BiboPublicationDate ofFullDate(int year, int month, int day) {
        return new BiboPublicationDate(year, month, day);
    }

    public int year() {
        return year;
    }

    public Optional<Integer> month() {
        return Optional.ofNullable(month);
    }

    public Optional<Integer> day() {
        return Optional.ofNullable(day);
    }

    public Optional<LocalDate> toLocalDate() {
        if (month == null || day == null) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.of(year, month, day));
    }

    public Literal toLiteral(ValueFactory valueFactory) {
        Objects.requireNonNull(valueFactory, "valueFactory");
        if (month != null && day != null) {
            return valueFactory.createLiteral(LocalDate.of(year, month, day));
        } else if (month != null) {
            String value = String.format("%04d-%02d", year, month);
            return valueFactory.createLiteral(value, XSD.GYEARMONTH);
        } else {
            String value = String.format("%04d", year);
            return valueFactory.createLiteral(value, XSD.GYEAR);
        }
    }

    private static int validateYear(int year) {
        if (year <= 0) {
            throw new IllegalArgumentException("year must be positive");
        }
        return year;
    }

    private static Integer validateMonth(Integer month) {
        if (month == null) {
            return null;
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        return month;
    }

    private static Integer validateDay(Integer day) {
        if (day == null) {
            return null;
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("day must be between 1 and 31");
        }
        return day;
    }
}
