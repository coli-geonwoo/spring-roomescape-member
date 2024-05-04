package roomescape.domain;

import roomescape.domain.builder.ReservationBuilder;
import roomescape.domain.builder.ReservationBuilderImpl;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {
    private final Long id;
    private final Name name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(
            final Long id,
            final Name name,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        validateDate(date);
        validateTime(date, time);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationBuilder builder() {
        return new ReservationBuilderImpl();
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("[ERROR] 잘못된 예약 날짜 입력입니다.");
        }
        LocalDate now = LocalDate.now();
        if (now.isAfter(date)) {
            throw new IllegalArgumentException("[ERROR] 현재 날짜보다 이전의 예약 날짜를 선택할 수 없습니다.");
        }
    }

    private void validateTime(final LocalDate date, final ReservationTime time) {
        LocalDate now = LocalDate.now();
        boolean isEqualDate = now.isEqual(date);
        if (isEqualDate && time.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("[ERROR] 현재 시간보다 이전의 시간을 선택할 수 없습니다.");
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name.getValue();
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public long getTimeId() {
        return time.getId();
    }

    public long getThemeId() {
        return theme.getId();
    }
}
