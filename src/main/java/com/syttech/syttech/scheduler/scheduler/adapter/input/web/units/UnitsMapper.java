package com.syttech.syttech.scheduler.scheduler.adapter.input.web.units;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.BusinessHour;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.Category;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.Professional;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.Service;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.UnitDetails;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.UnitSummary;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.UnitSummaryPage;
import com.syttech.syttech.scheduler.scheduler.domain.model.PageResult;
import com.syttech.syttech.scheduler.scheduler.domain.model.Unit;

/** DTO ↔ domain converters for the Units API. */
final class UnitsMapper {

    private UnitsMapper() {}

    private static URI toUri(final String s) {
        return s == null ? null : URI.create(s);
    }

    static UnitSummary toSummary(final Unit u) {
        return new UnitSummary()
                .id(u.id())
                .name(u.name())
                .address(u.address())
                .city(u.city())
                .coverImageUrl(toUri(u.coverImageUrl()));
    }

    static UnitDetails toDetails(final Unit u) {
        UnitDetails d =
                new UnitDetails()
                        .id(u.id())
                        .name(u.name())
                        .address(u.address())
                        .city(u.city())
                        .coverImageUrl(toUri(u.coverImageUrl()))
                        .phone(u.phone())
                        .email(u.email());
        Optional.ofNullable(u.businessHours())
                .orElse(List.of())
                .forEach(
                        h ->
                                d.addBusinessHoursItem(
                                        new BusinessHour()
                                                .dayOfWeek(
                                                        BusinessHour.DayOfWeekEnum.valueOf(
                                                                h.dayOfWeek().name()))
                                                .opensAt(h.opensAt().toString())
                                                .closesAt(h.closesAt().toString())));
        return d;
    }

    static UnitSummaryPage toPage(final PageResult<Unit> p) {
        UnitSummaryPage out =
                new UnitSummaryPage()
                        .page(p.page())
                        .size(p.size())
                        .totalElements(p.totalElements())
                        .totalPages(p.totalPages());
        p.content().forEach(u -> out.addContentItem(toSummary(u)));
        return out;
    }

    static Category toCategory(
            final com.syttech.syttech.scheduler.scheduler.domain.model.Category c) {
        return new Category().id(c.id()).name(c.name()).iconUrl(toUri(c.iconUrl()));
    }

    static Service toService(final com.syttech.syttech.scheduler.scheduler.domain.model.Service s) {
        return new Service()
                .id(s.id())
                .name(s.name())
                .description(s.description())
                .durationMinutes(s.durationMinutes())
                .price(s.price().doubleValue())
                .currency(s.currency());
    }

    static Professional toProfessional(
            final com.syttech.syttech.scheduler.scheduler.domain.model.Professional p) {
        return new Professional()
                .id(p.id())
                .name(p.name())
                .avatarUrl(toUri(p.avatarUrl()))
                .rating(p.rating());
    }
}
