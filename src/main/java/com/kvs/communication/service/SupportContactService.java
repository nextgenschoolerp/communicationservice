package com.kvs.communication.service;

import com.kvs.communication.dto.SupportContactRequest;
import com.kvs.communication.dto.SupportContactResponse;
import com.kvs.communication.entity.SupportContactSetting;
import com.kvs.communication.repo.SupportContactSettingRepository;
import com.kvs.core.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Contact support card: a school's own saved settings, or the deployment defaults (communication.support.*)
 * until the school saves its own.
 */
@Service
public class SupportContactService {

    private final SupportContactSettingRepository repository;
    private final SupportContactSetting defaults;
    private final ZoneId zone;

    public SupportContactService(SupportContactSettingRepository repository,
                                 @Value("${communication.support.email:}") String email,
                                 @Value("${communication.support.phone:}") String phone,
                                 @Value("${communication.support.open-days:MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY}") String openDays,
                                 @Value("${communication.support.opens-at:09:00}") String opensAt,
                                 @Value("${communication.support.closes-at:17:00}") String closesAt,
                                 @Value("${communication.support.response-time:Usually within 2 hours}") String responseTime,
                                 @Value("${communication.time-zone:Asia/Kolkata}") String timeZone) {
        this.repository = repository;
        this.defaults = SupportContactSetting.builder().email(email.trim()).phone(phone.trim()).openDays(openDays.trim())
                .opensAt(opensAt.trim()).closesAt(closesAt.trim()).responseTime(responseTime.trim()).build();
        this.zone = ZoneId.of(timeZone.trim());
    }

    @Transactional(readOnly = true)
    public SupportContactResponse get(String tenantId) {
        return map(repository.findByTenantId(tenantId).orElse(defaults));
    }

    @Transactional
    public SupportContactResponse save(SupportContactRequest r) {
        List<DayOfWeek> days = r.getOpenDays().stream().map(this::day).distinct().sorted().toList();
        LocalTime opens = time(r.getOpensAt(), "Opening time");
        LocalTime closes = time(r.getClosesAt(), "Closing time");
        if (!opens.isBefore(closes)) throw new BadRequestException("The closing time must be after the opening time");
        String email = r.getEmail() == null ? "" : r.getEmail().trim();
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new BadRequestException("Enter a valid email address");
        SupportContactSetting s = repository.findByTenantId(r.getTenantId())
                .orElseGet(() -> SupportContactSetting.builder().tenantId(r.getTenantId()).build());
        s.setEmail(email);
        s.setPhone(r.getPhone() == null ? "" : r.getPhone().trim());
        s.setOpenDays(String.join(",", days.stream().map(Enum::name).toList()));
        s.setOpensAt(opens.toString());
        s.setClosesAt(closes.toString());
        s.setResponseTime(r.getResponseTime().trim());
        return map(repository.save(s));
    }

    private DayOfWeek day(String name) {
        try {
            return DayOfWeek.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown day: " + name);
        }
    }

    private LocalTime time(String value, String label) {
        try {
            return LocalTime.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new BadRequestException(label + " must be a time like 08:00");
        }
    }

    private SupportContactResponse map(SupportContactSetting s) {
        List<DayOfWeek> days = Arrays.stream(s.getOpenDays().split(",")).map(String::trim).filter(x -> !x.isEmpty())
                .map(x -> DayOfWeek.valueOf(x.toUpperCase(Locale.ROOT))).sorted().toList();
        LocalTime opens = LocalTime.parse(s.getOpensAt());
        LocalTime closes = LocalTime.parse(s.getClosesAt());
        ZonedDateTime now = ZonedDateTime.now(zone);
        LocalTime t = now.toLocalTime();
        boolean online = days.contains(now.getDayOfWeek()) && !t.isBefore(opens) && t.isBefore(closes);
        String zoneName = DateTimeFormatter.ofPattern("zzz", Locale.ENGLISH).format(now);
        return SupportContactResponse.builder()
                .email(s.getEmail() == null ? "" : s.getEmail()).phone(s.getPhone() == null ? "" : s.getPhone())
                .hours(daysLabel(days) + " · " + opens + "–" + closes + " " + zoneName).responseTime(s.getResponseTime())
                .openDays(days.stream().map(Enum::name).toList()).opensAt(opens.toString()).closesAt(closes.toString())
                .timeZone(zone.getId()).online(online).build();
    }

    /** "Mon–Sat" for a run of consecutive days, otherwise "Mon, Wed, Fri". */
    private String daysLabel(List<DayOfWeek> days) {
        if (days.isEmpty()) return "Closed";
        boolean consecutive = true;
        for (int i = 1; i < days.size(); i++) if (days.get(i).getValue() != days.get(i - 1).getValue() + 1) consecutive = false;
        if (days.size() == 1) return short_(days.get(0));
        if (consecutive) return short_(days.get(0)) + "–" + short_(days.get(days.size() - 1));
        return String.join(", ", days.stream().map(this::short_).toList());
    }

    private String short_(DayOfWeek d) {
        return d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }
}
