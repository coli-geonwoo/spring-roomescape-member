package roomescape.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationDao {
    private static final String TABLE_NAME = "reservation";
    private static final String KEY_COLUMN_NAME = "id";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Reservation> reservationRowMapper;

    public ReservationDao(
            final JdbcTemplate jdbcTemplate,
            final DataSource source,
            final RowMapper<Reservation> reservationRowMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(source)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(KEY_COLUMN_NAME);
        this.reservationRowMapper = reservationRowMapper;
    }

    public Reservation save(final Reservation reservation) {
        try {
            SqlParameterSource params = makeInsertParams(reservation);
            long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
            return makeSavedReservation(reservation, id);
        } catch (DuplicateKeyException exception) {
            throw new IllegalStateException("[ERROR] 키 값 에러 : 중복된 예약 키가 존재합니다");
        }
    }

    public Optional<Reservation> findById(long id) {
        try {
            String sql =
                    "SELECT r.id as reservation_id, r.name, r.date, time.id as time_id, time.start_at as time_value, "
                            + "theme.id as theme_id, theme.name as theme_name, theme.description, theme.thumbnail "
                            + "FROM reservation as r "
                            + "INNER JOIN reservation_time as time "
                            + "ON r.time_id = time.id "
                            + "INNER JOIN theme as theme "
                            + "ON r.theme_id = theme.id "
                            + "WHERE r.id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, reservationRowMapper, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<Reservation> getAll() {
        String sql = "SELECT r.id as reservation_id, r.name, r.date, time.id as time_id, time.start_at as time_value, "
                + "theme.id as theme_id, theme.name as theme_name, theme.description, theme.thumbnail "
                + "FROM reservation as r "
                + "INNER JOIN reservation_time as time "
                + "ON r.time_id = time.id "
                + "INNER JOIN theme as theme "
                + "ON r.theme_id = theme.id ";
        return jdbcTemplate.query(sql, reservationRowMapper
        );
    }

    public List<Reservation> findByTimeId(final long timeId) {
        String sql = "SELECT r.id as reservation_id, r.name, r.date, time.id as time_id, time.start_at as time_value, "
                + "theme.id as theme_id, theme.name as theme_name, theme.description, theme.thumbnail "
                + "FROM reservation as r "
                + "INNER JOIN reservation_time as time "
                + "ON r.time_id = time.id "
                + "INNER JOIN theme as theme "
                + "ON r.theme_id = theme.id "
                + "WHERE r.time_id = ?";
        return jdbcTemplate.query(sql, reservationRowMapper, timeId);
    }

    public List<Reservation> findByThemeId(final long themeId) {
        String sql = "SELECT r.id as reservation_id, r.name, r.date, time.id as time_id, time.start_at as time_value, "
                + "theme.id as theme_id, theme.name as theme_name, theme.description, theme.thumbnail "
                + "FROM reservation as r "
                + "INNER JOIN reservation_time as time "
                + "ON r.time_id = time.id "
                + "INNER JOIN theme as theme "
                + "ON r.theme_id = theme.id "
                + "WHERE r.theme_id = ?";
        return jdbcTemplate.query(sql, reservationRowMapper, themeId);
    }


    public List<Long> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        String sql = "SELECT time_id FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?";
        return jdbcTemplate.query(
                sql, (resultSet, rowNum) -> resultSet.getLong("time_id"),
                date.toString(), timeId, themeId
        );
    }

    public List<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId) {
        String sql = "SELECT time_id FROM reservation WHERE date = ? AND theme_id = ?";
        return jdbcTemplate.query(
                sql, (resultSet, rowNum) -> resultSet.getLong("time_id"),
                date.toString(), themeId
        );
    }

    public List<Long> findRanking(final LocalDate from, final LocalDate to, final int count) {
        String sql = "SELECT theme_id, count(*) AS count FROM reservation WHERE date BETWEEN ? AND ? GROUP BY theme_id ORDER BY count DESC LIMIT ?";
        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> resultSet.getLong("theme_id"),
                from, to, count
        );
    }

    public void delete(final long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", Long.valueOf(id));
    }

    private Reservation makeSavedReservation(Reservation reservation, long id) {
        return Reservation.builder()
                .id(id)
                .name(reservation.getName())
                .date(reservation.getDate())
                .time(reservation.getTime())
                .theme(reservation.getTheme())
                .build();
    }

    private MapSqlParameterSource makeInsertParams(Reservation reservation) {
        return new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", reservation.getDate().toString())
                .addValue("time_id", String.valueOf(reservation.getTimeId()))
                .addValue("theme_id", String.valueOf(reservation.getThemeId()));
    }
}
